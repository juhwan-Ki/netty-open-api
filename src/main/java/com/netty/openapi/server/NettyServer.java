package com.netty.openapi.server;

import com.netty.openapi.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NettyServer {
    private static final Logger logger = LogManager.getLogger(NettyServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void startServer(int port) throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // bossGroup -> 클라이언트와 연결 처리
                .handler(new LoggingHandler(LogLevel.INFO)) // 서버 소켓 채널에 필요한 핸들러 추가
                .childHandler(new ChannelInitializer<SocketChannel>() { // workerGroup -> 클라이언트의 요청 처리
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception { // 클라이언트 응답에 필요한 핸들러 추가
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8), new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast(new MessageCodec());
                        pipeline.addLast(new RouterHandler());
//                        pipeline.addLast(new ApiCallHandler());  // 동기 방식
//                        pipeline.addLast(new NioApiCallHandler()); // 비동기 방식
                        pipeline.addLast(new NioOpenApiCallHandler()); // Netty로 Http 비동기 호출 방식
                    }
                })
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        // netty 서버 바인딩
        ChannelFuture future = bootstrap.bind(port).sync();
        logger.info("netty server started on port {}", port);
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        logger.info("Netty server stopped.");
    }
}
