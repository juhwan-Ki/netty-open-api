package com.netty.openapi.client;

import com.netty.openapi.client.handler.MessageEncoder;
import com.netty.openapi.client.handler.ResultHandler;
import com.netty.openapi.dto.RequestDto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NettyClient {
    private static final Logger logger = LogManager.getLogger(NettyClient.class);
    private EventLoopGroup loopGroup;

    public static void main(String[] args) {
        NettyClient client = new NettyClient();
        client.startClient("127.0.0.1", 8081);
    }

    public void startClient(String host, int port) {
        loopGroup = new NioEventLoopGroup();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8), new StringDecoder(StandardCharsets.UTF_8));
                            socketChannel.pipeline().addLast(new MessageEncoder()); // 서버로 전송할 데이터를 Encoding
                            socketChannel.pipeline().addLast(new ResultHandler());
                        }
                    });

            // TODO : 다른 방법으로 클라이언트를 생성하도록 변경 필요
            for (int i = 0; i < 10; i++) {
                ChannelFuture future = bootstrap.connect(host, port);
                int cnt = i;
                String url;
                if(cnt / 2 == 0) url = "/api/rentals";
                else url = "/api/sales";
                future.addListener( new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()) {
                            RequestDto req = sendRequest(url);
                            logger.info("{} 번째 Request : {}", cnt, req);
                            // 메시지 전송
                            channelFuture.channel().writeAndFlush(req);
                        }
                    }
                });
            }
    }

    public void stopClient() {
        loopGroup.shutdownGracefully();
    }

    private RequestDto sendRequest(String url) throws IOException {
        int ranNum = (int) ( Math.random() * 3 + 1);
        // 1번 일때는 healthCheck 2번은 apiCall 3번 close
        logger.info("ranNum: {}", ranNum);
        if(ranNum == 1 || ranNum == 3) {
            return new RequestDto(String.valueOf(ranNum));
        }
        else if(ranNum == 2) {
            return new RequestDto(String.valueOf(ranNum), url);
        }
        else {
            throw new IllegalArgumentException("Invalid ranNum");
        }
    }
}
