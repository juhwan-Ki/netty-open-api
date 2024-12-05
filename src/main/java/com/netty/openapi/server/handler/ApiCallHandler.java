package com.netty.openapi.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.dto.RequestDto;
import com.netty.openapi.dto.ResponseDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiCallHandler extends SimpleChannelInboundHandler<RequestDto> {
    private static final Logger logger = LogManager.getLogger(ApiCallHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestDto req) throws Exception {
        ctx.writeAndFlush(callOpenApi(req));
    }

    // http로 tomcat api 호출
    private ApiResponse<ResponseDto> callOpenApi(RequestDto req) {
        HttpURLConnection conn = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("http://localhost:8080" + req.getReqUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            writer =  new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
            // 객체를 JSON으로 변환 후 전송
            String json = mapper.writeValueAsString(req);
            writer.write(json); // JSON 데이터를 전송
            writer.flush();

            int respCode = conn.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                return ApiResponse.ok(mapper.readValue(reader, ResponseDto.class));
            } else {
                return ApiResponse.error(conn.getResponseMessage());
            }
        } catch (Exception e) {
            logger.error("api call failed : (cause : {})", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception e) {}
            if (reader != null) try { reader.close(); } catch (Exception e) {}
            if (conn != null) conn.disconnect();
        }
    }
}
