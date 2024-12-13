package com.netty.openapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Constants {
    public static final String HOST = "127.0.0.1";
    public static final String API_HOST ="apis.data.go.kr";
    public static final String API_BASE_URL ="https://apis.data.go.kr/1613000/HWSPR02";
    public static final String API_RENTAL_URL = "/rsdtRcritNtcList";
    public static final String API_SALES_URL = "/ltRsdtRcritNtcList";
    public static final int HTTP_PORT = 8080;
    public static final int HTTPS_PORT = 443;
    public static final int TCP_PORT = 8081;
    public static final ObjectMapper MAPPER = new ObjectMapper();
}
