package com.netty.openapi.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.common.Constants;
import com.netty.openapi.dto.ResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private static final Logger logger = LogManager.getLogger(HttpResponseHandler.class);
    private final ChannelHandlerContext ctx; // TCP 채널의 ctx

    public HttpResponseHandler(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    protected void channelRead0(ChannelHandlerContext innerCtx, FullHttpResponse response) throws Exception {
        String resp = getApiResponse(response);
        logger.info("Received response: {}", resp);
        // TCP 채널 ctx를 사용해서 클라이언트에게 응답 전달
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext innerCtx, Throwable cause) {
        logger.error("Error processing HTTP response", cause);
        ctx.writeAndFlush(ApiResponse.error("Error processing HTTP response"));
    }

    private String getApiResponse(FullHttpResponse response) throws IOException {
        String resp = response.content().toString(StandardCharsets.UTF_8);
        logger.info("api reps : {}",resp);

        JsonNode rootNode = Constants.MAPPER.readTree(resp);
        // body 이후의 값만 넘기도록 변경
        JsonNode headerNode = rootNode.path("response").path("header");
        String resultCode = headerNode.path("resultCode").asText();
        String resultMsg = headerNode.path("resultMsg").asText();
        // 정상적인 경우에는 데이터만 넘김
        if (resultCode.equals("00")) {
            JsonNode bodyNode = rootNode.path("response").path("body");
            JsonNode itemNode = bodyNode.path("item");
            String totalCount = bodyNode.path("totalCount").asText();
            String numOfRows = bodyNode.path("numOfRows").asText();
            String pageNo = bodyNode.path("pageNo").asText();
            return Constants.MAPPER.writeValueAsString(
                    ApiResponse.ok(new ResponseDto.Builder().totalCount(totalCount).numOfRows(numOfRows).pageNo(pageNo).data(itemNode.toString()).build()));
        } else {
            return Constants.MAPPER.writeValueAsString(ApiResponse.error(resultMsg));
        }
    }
}
