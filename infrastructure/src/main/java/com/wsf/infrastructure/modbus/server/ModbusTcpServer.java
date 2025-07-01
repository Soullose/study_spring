package com.wsf.infrastructure.modbus.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ModbusTcpServer {
    private final int port;
    private final CustomModbusHandler requestHandler;

    public ModbusTcpServer(int port, CustomModbusHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    // 1. 处理 TCP 粘包/拆包
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 4, 2, 0, 0))
                                    // 2. 自定义 Modbus 解码器
                                    .addLast(new ModbusTcpDecoder())
                                    // 3. 自定义业务处理器
                                    .addLast(requestHandler)
                                    // 4. Modbus 响应编码器
                                    .addLast(new ModbusTcpEncoder());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Modbus TCP 服务端已启动，监听端口：" + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 自定义请求处理器（需实现业务逻辑）
        CustomModbusHandler handler = new CustomModbusHandler();
        new ModbusTcpServer(502, handler).start();
    }
}
