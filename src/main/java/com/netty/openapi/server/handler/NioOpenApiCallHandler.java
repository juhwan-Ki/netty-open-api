package com.netty.openapi.server.handler;

import com.netty.openapi.common.ApiKeyManager;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.common.Constants;
import com.netty.openapi.dto.RequestDto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NioOpenApiCallHandler extends SimpleChannelInboundHandler<RequestDto> {
    private static final Logger logger = LogManager.getLogger(NioOpenApiCallHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestDto req) throws Exception {
        logger.info("Received request: {}", req);
        openApiCall(ctx, req);
    }

    private void openApiCall(ChannelHandlerContext ctx, RequestDto req) throws SSLException {
        Bootstrap httpBootStrap = new Bootstrap();
        // SSL 설정
        SslContext sslContext = SslContextBuilder.forClient().build();
        httpBootStrap.group(ctx.channel().eventLoop()) // 서버와 같은 eventLoop를 사용하여 추가적인 스레드를 생성하지 않음
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // SSL 핸들러 추가
                        pipeline.addLast(sslContext.newHandler(socketChannel.alloc(), Constants.API_HOST, Constants.HTTPS_PORT));
                        pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(65536));
                        pipeline.addLast(new HttpResponseHandler(ctx));
                    }
                });
        // http로 open api 호출
        httpBootStrap.connect(Constants.API_HOST, Constants.HTTPS_PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // 성공했을 때 응답 처리
                String url = getBaseUrl(req);
                if(future.isSuccess()) {
                    logger.info("Successfully connected");
                    FullHttpRequest request = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1, // HTTP 버전 1,1
                            HttpMethod.GET,      // Http Method
                            url       // 요청 url
                    );
                    // 헤더 설정
                    request.headers().set(HttpHeaderNames.HOST, Constants.API_HOST);
                    request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
//                    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    // Http 채널에 비동기 요청
                    logger.info("Sending request: {}", request);
                    future.channel().writeAndFlush(request);
                } else {
                    logger.error("Failed to connect");
                    // TCP 채널에 데이터 전송
                    ctx.writeAndFlush(Constants.MAPPER.writeValueAsString(ApiResponse.error("Failed to connect")));
                }
            }
        });
    }

    private String getBaseUrl(RequestDto req) throws IOException {
        StringBuilder builder = new StringBuilder(Constants.API_BASE_URL + req.getReqUrl()
                + "?serviceKey=" + ApiKeyManager.getApiKey());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("pageNo", req.getPageNo());

        String queryString = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null) // null 값 제외
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        if (!queryString.isEmpty())
            builder.append("&").append(queryString);

        return builder.toString();
    }
}

