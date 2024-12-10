package com.netty.openapi.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResultHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LogManager.getLogger(ResultHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 서버에서 받은 응답을 로그로 출력
        logger.info("Received response from server: {}", msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error in client", cause);
        ctx.close();
    }
}