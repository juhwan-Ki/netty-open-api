package com.netty.openapi.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.dto.RequestDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ServerCustomDecoder extends MessageToMessageDecoder<String> {
    private final ObjectMapper mapper = new ObjectMapper();

    // 네트워크에서 받은 데이터를 변환(클라이언트에서 넘어온 데이터를 변환)
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> out) throws Exception {
        // jsonToRequestDto
        RequestDto requestDto = mapper.readValue(s, RequestDto.class);
        out.add(requestDto);
    }
}
