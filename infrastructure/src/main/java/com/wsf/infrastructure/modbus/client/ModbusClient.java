package com.wsf.infrastructure.modbus.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ModbusClient {
	private final EventLoopGroup group;
	private final Bootstrap bootstrap;
	private final ModbusClientHandler handler;
	private final AtomicInteger transactionIdGenerator = new AtomicInteger(1);
	private Channel channel;

	public ModbusClient() {
		this.group = new NioEventLoopGroup();
		this.handler = new ModbusClientHandler();
		this.bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new ModbusEncoder());
						pipeline.addLast(new ModbusDecoder());
						pipeline.addLast(handler);
					}
				});
	}

	// 连接到Modbus服务器
	public CompletableFuture<Channel> connect(String host, int port) {
		CompletableFuture<Channel> future = new CompletableFuture<>();

		bootstrap.connect(host, port).addListener((ChannelFuture channelFuture) -> {
			if (channelFuture.isSuccess()) {
				this.channel = channelFuture.channel();
				future.complete(channel);
			} else {
				future.completeExceptionally(channelFuture.cause());
			}
		});

		return future;
	}

	// 读取Holding Registers (功能码03)
	public CompletableFuture<int[]> readHoldingRegisters(int unitId, int startAddress, int quantity) {
		if (channel == null || !channel.isActive()) {
			return CompletableFuture.failedFuture(new IllegalStateException("Client not connected"));
		}

		int transactionId = transactionIdGenerator.getAndIncrement();
		ModbusRequest request = new ModbusRequest(transactionId, unitId, 0x03, startAddress, quantity);

		return handler.sendRequest(channel, request).thenApply(ModbusResponse::getRegisterValues);
	}


	// 检查连接状态
	public boolean isConnected() {
		return channel != null && channel.isActive();
	}

	private final ConcurrentMap<Integer, ScheduledFuture<?>> periodicTasks = new ConcurrentHashMap<>();
	private final AtomicInteger taskIdGenerator = new AtomicInteger(0);


	// 修改断开连接方法，清理所有周期任务
	public CompletableFuture<Void> disconnect() {
		// 取消所有周期性任务
		periodicTasks.values().forEach(future -> future.cancel(false));
		periodicTasks.clear();

		CompletableFuture<Void> future = new CompletableFuture<>();
		// ... 原有断开连接逻辑保持不变 ...

		if (channel != null && channel.isActive()) {
			channel.close().addListener(channelFuture -> {
				group.shutdownGracefully().addListener(groupFuture -> {
					if (groupFuture.isSuccess()) {
						future.complete(null);
					} else {
						future.completeExceptionally(groupFuture.cause());
					}
				});
			});
		} else {
			group.shutdownGracefully().addListener(groupFuture -> {
				if (groupFuture.isSuccess()) {
					future.complete(null);
				} else {
					future.completeExceptionally(groupFuture.cause());
				}
			});
		}

		return future;
	}

	// 使用示例
	public static void main(String[] args) {
		ModbusClient client = new ModbusClient();

		try {
			// 连接到Modbus服务器
			client.connect("127.0.0.1", 502).get();
			System.out.println("连接成功");

			// 读取Holding Registers
			// 从地址0开始读取10个寄存器，设备ID为1
			int[] values = client.readHoldingRegisters(1, 0, 10).get();
			System.out.println("读取的寄存器值:");
			for (int i = 0; i < values.length; i++) {
				System.out.printf("地址 %d: %d (0x%04X)\n", i, values[i], values[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 断开连接
			try {
				client.disconnect().get();
				System.out.println("连接已断开");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
