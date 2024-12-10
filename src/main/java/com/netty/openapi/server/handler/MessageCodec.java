package com.netty.openapi.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.dto.RequestDto;
import com.netty.openapi.dto.ResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class MessageCodec extends MessageToMessageCodec<String, ApiResponse<ResponseDto>> {
    private final ObjectMapper mapper = new ObjectMapper();

    // 서버 데이터 변환(DTO -> JSON)
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ApiResponse<ResponseDto> resp, List<Object> list) throws Exception {
        list.add(mapper.writeValueAsString(resp));
    }

    // 클라이언트에서 넘어온 데이터 변환(json -> DTO)
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        list.add(mapper.readValue(s, RequestDto.class));
    }
}
