package com.netty.openapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.dto.RequestDto;
import com.netty.openapi.dto.ResponseDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@WebServlet("/api/*")
public class OpenApiCall extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(OpenApiCall.class);
    private String apiKey;

    @Override
    public void init() throws ServletException {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("api-key.properties"));
            apiKey = properties.getProperty("api-key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from properties file", e);
        }

        if (apiKey == null)
            throw new ServletException("API Key not found");
    }

    // 공공 데이터 가져오기
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        logger.trace("uri: {}", uri);

        // 공공 임대 주택
        if (uri.endsWith("rentals"))
            fetchAndCacheData("/rsdtRcritNtcList", request, response);
        // 공공 분양 주택
        else if(uri.endsWith("sales"))
            fetchAndCacheData("/ltRsdtRcritNtcList",request, response);
        else{
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(new ResponseDto.Builder().status("error").message("Invalid URI").build()));
        }
    }

    // OpenAPI 데이터 가져오기
    private void fetchAndCacheData(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream in = request.getInputStream();
        RequestDto requestDto = null;
        if (in.available() > 0)
            requestDto = new ObjectMapper().readValue(in, RequestDto.class);
        // TODO: url 컨버팅?
        URL apiUrl = new URL(getBaseUrl(url, apiKey, requestDto));

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        BufferedOutputStream out = null;
        ResponseDto resp;
        try {
            conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            // 정상적으로 데이터를 조회
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    builder.append(inputLine);
                }
                // TODO: json 데이터를 어떻게 넘길까??
                resp = new ResponseDto.Builder().status("success").data(builder.toString()).build();
                // TODO: 전송하기 전에 캐싱 하는 기능 추가해야함
            } else
                resp = new ResponseDto.Builder().status("error").message(conn.getResponseMessage()).build();

            // 데이터를 클라이언트로 전송
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out = new BufferedOutputStream(response.getOutputStream());
            String respJson = new ObjectMapper().writeValueAsString(resp);
            out.write(respJson.getBytes());
        }
        finally {
            if (reader != null ) try { reader.close(); } catch (Exception e) {}
            if (out != null ) try { out.close(); } catch (Exception e) {}
            if (conn != null ) conn.disconnect();
        }
    }

    private String getBaseUrl(String url, String apiKey,RequestDto request) {
        String baseUrl = "https://apis.data.go.kr/1613000/HWSPR02";
        StringBuilder builder = new StringBuilder().append(baseUrl).append(url).append("?serviceKey=").append(apiKey);

        if (request != null) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("brtcCode", request.getBrtcCode());
            params.put("signguCode", request.getSignguCode());
            params.put("pageNo", request.getPageNo());
            params.put("yearMtBegin", request.getYearMtBegin());
            params.put("yearMtEnd", request.getYearMtEnd());

            String queryString = params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null) // null 값 제외
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&")); // "&"로 연결

            if (!queryString.isEmpty())
                builder.append(queryString);
        }

        return builder.toString();
    }
}
