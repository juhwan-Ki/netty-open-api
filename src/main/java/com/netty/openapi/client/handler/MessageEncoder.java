package com.netty.openapi.client.handler;

import com.netty.openapi.common.Constants;
import com.netty.openapi.dto.RequestDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class MessageEncoder extends MessageToMessageEncoder<RequestDto> {

    // 서버로 보낼Json 생성(dto -> json)
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestDto req, List<Object> list) throws Exception {
        list.add(Constants.MAPPER.writeValueAsString(req));
    }
}
