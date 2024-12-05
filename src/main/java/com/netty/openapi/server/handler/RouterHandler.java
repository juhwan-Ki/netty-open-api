package com.netty.openapi.server.handler;

import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.dto.RequestDto;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouterHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(RouterHandler.class);

    // 클라이언트와 연결이 되었을 경우 호출
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(ApiResponse.ok("connection success"));
    }

    // TODO : ByteBuf 사용 해보도록 해보자
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestDto req = (RequestDto) msg;
        switch (req.getReqNo()) {
            case "1": // connection close 요청
                ctx.writeAndFlush(ApiResponse.ok("connection closed"));
                ctx.close();
                break;
            case "2": // health check 요청
                ctx.writeAndFlush(ApiResponse.ok("server is healthy"));
                break;
            case "3": // api call
                ctx.fireChannelRead(req);
                break;
        }
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        if (ctx.channel().isActive()) {
//        }
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("handler Error : {}", cause.getMessage());
        ctx.writeAndFlush(ApiResponse.error(cause.getMessage()));
    }
}
