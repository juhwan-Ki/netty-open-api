package com.netty.openapi.server.handler;

import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.dto.RequestDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouterHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(RouterHandler.class);

    // 클라이언트와 연결이 되었을 경우 호출
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connection success");
        ctx.writeAndFlush(ApiResponse.ok("connection success"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestDto req = (RequestDto) msg;
        logger.info("msg : {}", msg);

        switch (req.getReqNo()) {
            case "1": // health check 요청
                ctx.writeAndFlush(ApiResponse.ok("server is healthy"));
                logger.info("server is healthy");
                break;
            case "2": // api call
                ctx.fireChannelRead(req);
                break;
            case "3": // connection close 요청
                ctx.writeAndFlush(ApiResponse.ok("connection closed"));
                ctx.channel().close();
                logger.info("connection closed");
                break;
        }
    }

    // 핸들러에서 에러 발생 시 호출
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("handler Error : {}", cause.getMessage());
    }
}
