package com.netty.openapi.server.handler;

import com.netty.openapi.dto.RequestDto;
import com.netty.openapi.dto.ResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RouterHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(RouterHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestDto req = (RequestDto) msg;
        switch (req.getReqNo()) {
            case "1": // connection 연결 요청
                new ResponseDto.Builder().status("success").message("connection success").build();
                break;
            case "2": // connection close 요청
                new ResponseDto.Builder().status("success").message("connection closed").build();
                ctx.close();
                break;
            case "3": // health check 요청
                new ResponseDto.Builder().status("success").message("health check").build();
                break;
            case "4": // api call
                ctx.fireChannelRead(req);
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        ctx.writeAndFlush(new ResponseDto.Builder().status("error").message(cause.getMessage()).build());
    }
}
