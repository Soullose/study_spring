package com.wsf.infrastructure.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * github-HuLa
 */
@ChannelHandler.Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	private static final Logger log = LoggerFactory.getLogger(NettyWebSocketServerHandler.class);
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		super.handlerAdded(ctx);
		log.debug("客户端连接后，触发该方法");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		/// 可能出现业务判断离线后再次触发 channelInactive
		log.warn("[{}]掉线", ctx.channel().id());
		userOffline(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent idleStateEvent) {
			// 读空闲
			if (idleStateEvent.state() == IdleState.READER_IDLE) {
				// 关闭用户的连接
				userOffline(ctx);
			}
		} else if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
//			webSocketService.connect(ctx.channel());
//			String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
//			if (StringUtils.isNotBlank(token)) {
//				webSocketService.authorize(ctx.channel(), new WSAuthorize(token));
//			}
//			log.info("握手成功：{}", (Long)NettyUtil.getAttr(ctx.channel(), NettyUtil.UID));
			log.debug("Handshake 已成功完成，通道已升级到 websockets");
		}
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.warn("异常发生，异常消息", cause);
		ctx.channel().close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		log.debug("收到消息：{}", msg.text());
	}

	private void userOffline(ChannelHandlerContext ctx) {
//		webSocketService.remove(ctx.channel());
		ctx.channel().close();
	}
}
