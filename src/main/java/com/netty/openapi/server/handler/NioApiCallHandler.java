package com.netty.openapi.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.common.Constants;
import com.netty.openapi.dto.RequestDto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class NioApiCallHandler extends SimpleChannelInboundHandler<RequestDto> {
    private static final Logger logger = LogManager.getLogger(NioApiCallHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestDto req) throws Exception {
        logger.info("Received request: {}", req);
        openApiCall(ctx, req);
    }

    private void openApiCall(ChannelHandlerContext ctx, RequestDto req) {
        Bootstrap httpBootStrap = new Bootstrap();
        httpBootStrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(65536));
                        // Api call Response -> 다른데서 재사용할 필요가 없어 보여서 익명 클래스로 작성함
                        pipeline.addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse response) throws Exception {
                                 String resp = response.content().toString(StandardCharsets.UTF_8);
                                 logger.info("Received response: {}", resp);
                                 // TCP 채널 ctx를 사용해서 클라이언트에게 응답 전달
                                ctx.writeAndFlush(resp);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext innerCtx, Throwable cause) {
                                logger.error("Error processing HTTP response", cause);
                            }
                        });
                    }
                });
        // http로 tomcat api 호출
        httpBootStrap.connect(Constants.HOST, Constants.HTTP_PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                // 성공했을 때 응답 처리
                if(future.isSuccess()) {
                    logger.info("Successfully connected");
                    FullHttpRequest request = new DefaultFullHttpRequest(
                            HttpVersion.HTTP_1_1, // HTTP 버전 1,1
                            HttpMethod.POST,      // Http Method
                            req.getReqUrl()       // 요청 url
                    );
                    ByteBuf buffer = Unpooled.copiedBuffer(Constants.MAPPER.writeValueAsString(req), StandardCharsets.UTF_8);
                    // 헤더 설정
                    request.headers().set(HttpHeaderNames.HOST, Constants.HOST);
                    request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    request.headers().set(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
                    // 바디 설정
                    request.content().writeBytes(buffer);
                    // Http 채널에 비동기 요청
                    logger.info("Sending request: {}", request);
                    future.channel().writeAndFlush(request);
                } else {
                    logger.error("Failed to connect");
                    // TCP 채널에 데이터 전송
                    ctx.writeAndFlush(Constants.MAPPER.writeValueAsString(ApiResponse.error(future.cause().getMessage())));
                }
            }
        });
    }
}
