package com.wsf.infrastructure.websocket;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(HttpHeadersHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.error("HttpHeadersHandler: {}", msg);
        if (msg instanceof FullHttpRequest request) {
            URI uri = new URI(request.uri());
            String realPath = uri.getPath();
            
            log.debug("request-url:{}", request.uri());
            log.debug("real-path:{}", realPath);
            
            HttpHeaders headers = request.headers();
            String ip = getIpAddr(request);
            log.debug("ip: {}", ip);
            
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(request);
        } else {
            ctx.fireChannelRead(msg);
        }
    }


    public static String getIpAddr(FullHttpRequest request) {
        if (request == null) {
            return "unknown";
        }
        HttpHeaders headers = request.headers();
        String ip = headers.get("x-forwarded-for");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("X-Forwarded-For");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("X-Real-IP");
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : "";
    }
}