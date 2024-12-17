package com.netty.openapi.server.handler;

import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.common.Constants;
import com.netty.openapi.dto.RequestDto;
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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestDto req) throws Exception {
        logger.info("Received request: {}", req);
        String response = callOpenApi(req);
        logger.info("Response received: {}", response);

        ctx.writeAndFlush(response);
    }

    // http로 tomcat api 호출
    private String callOpenApi(RequestDto req) throws IOException {
        HttpURLConnection conn = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("http://localhost:8080" + req.getReqUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            writer =  new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
            // 객체를 JSON으로 변환 후 전송
            String json = Constants.MAPPER.writeValueAsString(req);
            writer.write(json); // JSON 데이터를 전송
            writer.flush();

            // 결과 받아오기
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            // 결과 리턴
            return builder.toString();

        } catch (Exception e) {
            logger.error("api call failed : (cause : {})", e.getMessage());
            return Constants.MAPPER.writeValueAsString(ApiResponse.error(e.getMessage()));
        } finally {
            if (writer != null) try { writer.close(); } catch (Exception ignored) {}
            if (reader != null) try { reader.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }
}
