package com.netty.openapi.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class NettyServerInitializer implements ServletContextListener {
    private NettyServer nettyServer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        nettyServer = new NettyServer();
        new Thread(() -> {
            try {
                nettyServer.startServer(8081);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (nettyServer != null) {
            nettyServer.stop(); // Netty 서버 종료
        }
    }
}
