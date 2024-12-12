package com.netty.openapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Constants {
    public static final String HOST = "127.0.0.1";
    public static final int HTTP_PORT = 8080;
    public static final int TCP_PORT = 8081;
    public static final ObjectMapper MAPPER = new ObjectMapper();
}
