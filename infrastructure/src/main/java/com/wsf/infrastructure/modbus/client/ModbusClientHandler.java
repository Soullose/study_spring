package com.wsf.infrastructure.modbus.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ModbusClientHandler extends SimpleChannelInboundHandler<ModbusResponse> {

    private final Map<Integer, CompletableFuture<ModbusResponse>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ModbusResponse response) {
        CompletableFuture<ModbusResponse> future = pendingRequests.remove(response.getTransactionId());
        if (future != null) {
            if (response.isError()) {
                future.completeExceptionally(new RuntimeException(
                        "Modbus error: " + response.getErrorCode()));
            } else {
                future.complete(response);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // 完成所有待处理的请求为异常
        pendingRequests.values().forEach(future -> future.completeExceptionally(cause));
        pendingRequests.clear();
        ctx.close();
    }

    public CompletableFuture<ModbusResponse> sendRequest(Channel channel, ModbusRequest request) {
        CompletableFuture<ModbusResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.getTransactionId(), future);

        channel.writeAndFlush(request).addListener(channelFuture -> {
            if (!channelFuture.isSuccess()) {
                pendingRequests.remove(request.getTransactionId());
                future.completeExceptionally(channelFuture.cause());
            }
        });

        return future;
    }
}
