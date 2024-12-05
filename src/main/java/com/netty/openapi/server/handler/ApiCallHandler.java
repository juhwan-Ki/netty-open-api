package com.netty.openapi.server.handler;

import com.netty.openapi.dto.RequestDto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiCallHandler extends SimpleChannelInboundHandler<RequestDto> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestDto requestDto) throws Exception {

    }


}
