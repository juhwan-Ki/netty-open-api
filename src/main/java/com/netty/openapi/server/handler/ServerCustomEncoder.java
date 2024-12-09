package com.netty.openapi.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.dto.ResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ServerCustomEncoder extends MessageToMessageEncoder<ApiResponse<ResponseDto>> {
    private static final Logger logger = LogManager.getLogger(ServerCustomEncoder.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 서버 데이터 형식을 Json으로 변경
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ApiResponse<ResponseDto> apiResponse, List<Object> out) throws Exception {
        String result = objectMapper.writeValueAsString(apiResponse);
        logger.info("data : {}", result);
        out.add(result);
    }
}
