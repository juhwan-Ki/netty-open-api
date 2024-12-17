package com.netty.openapi.client;

import com.netty.openapi.client.handler.MessageEncoder;
import com.netty.openapi.client.handler.ResultHandler;
import com.netty.openapi.common.Constants;
import com.netty.openapi.dto.RequestDto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final Logger logger = LogManager.getLogger(NettyClient.class);
    // 현재는 간단하게 하기 위해 Map을 사용 -> 더 효율적인 다중 처리를 하려면 ChannelGroup을 사용할 수 있음
    private final Map<Integer, Integer> connectionMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        new NettyClient().startClient();
    }

    public void startClient() {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
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
                })
                .option(ChannelOption.SO_KEEPALIVE, true); // 연결을 유지하도록

        // n개 클라이언트 생성
        for (int i = 1; i <= 10; i++) {
            clientConnect(bootstrap, i);
        }
    }

    private void clientConnect(Bootstrap bootstrap, int clientId) {
        if(!connectionMap.containsKey(clientId)) {
            connectionMap.put(clientId, 0);
        }
        // connection 요청
        bootstrap.connect(Constants.HOST, Constants.TCP_PORT)
                .addListener( new ChannelFutureListener() { // 작업이 완료될 시 호출할 리스너를 등록
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        // 연결 성공 시 tcp 요청
                        if(channelFuture.isSuccess()) {
                            tcpConnect(channelFuture.channel(), clientId);
                        } else {
                            logger.error("connect failed : {}", channelFuture.cause().getMessage());
                            logger.info("client reconnecting...");
                            tcpReconnect(bootstrap, clientId);
                        }
            }
        });
    }

    // 주기적으로 tcp 요청을 처리
    private void tcpConnect(Channel channel, int clientId) {
        // scheduleAtFixedRate(Runnable, initialDelay, period, time) -> 일정 시간마다 주기적으로 요청하는 스케줄러
        ScheduledFuture<?> scheduledTask = channel.eventLoop().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestDto req = sendRequest(clientId);
                    logger.info("Request : {}",  req);
                    // 메시지 전송
                    channel.writeAndFlush(req);
                } catch (Exception e) {
                    logger.error("client id {} request failed : {} ", clientId, e.getMessage());
                }
            }
        }, 0, 1, TimeUnit.MINUTES);

        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                logger.info("client not active : clientId={}", clientId);
                scheduledTask.cancel(false);
            }
        });
    }

    // 서버에서 오류나면 다시 연결
    private void tcpReconnect(Bootstrap bootstrap, int clientId) {
        int reconnectCnt = connectionMap.get(clientId);
        // 3회가 넘어가면 연결 끊어버림
        if(reconnectCnt > 3) {
            logger.info("client ID : {} closed", clientId);
            connectionMap.remove(clientId);
            // 모든 connection이 끊어졌으면 서버 종료
            if(connectionMap.isEmpty()) {
                logger.info("netty client server closed");
                bootstrap.config().group().shutdownGracefully();
            }
            return;
        }
        // 30초 후 재연결
        logger.info("client ID : {} reconnect cnt : {}", clientId, reconnectCnt);
        connectionMap.put(clientId, ++reconnectCnt);
        bootstrap.config().group().schedule(new Runnable() {
            @Override
            public void run() {
                clientConnect(bootstrap, clientId);
            }
        }, 30, TimeUnit.SECONDS);
    }

    private RequestDto sendRequest(int clientId) {
        int ranNum = (int) (Math.random() * 3 + 1);
        String pageNo = String.valueOf((int) (Math.random() * 9 + 1));
        // 1 : healthCheck, 3 : close
        if(ranNum == 1 || ranNum == 3) {
            return new RequestDto(String.valueOf(ranNum));
        }
        // apiCall
        else if(ranNum == 2) {
            return new RequestDto(String.valueOf(ranNum), clientId / 2 == 0 ? Constants.API_RENTAL_URL : Constants.API_SALES_URL, pageNo);
        }
        // error
        else {
            throw new IllegalArgumentException("Invalid ranNum");
        }
    }
}
