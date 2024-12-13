package com.netty.openapi.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netty.openapi.common.ApiResponse;
import com.netty.openapi.common.Constants;
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
import java.nio.charset.StandardCharsets;
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
        InputStream in = null;
        try {
            properties.load(in = getClass().getClassLoader().getResourceAsStream("api-key.properties"));
            apiKey = properties.getProperty("api-key");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from properties file", e);
        } finally {
            if (in != null) try { in.close(); } catch (Exception e) {}
        }

        if (apiKey == null)
            throw new ServletException("API Key not found");
    }

    // Netty에서 http 호출 처리
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String uri = request.getRequestURI();
        logger.trace("url: {}", uri);

        // 공공 임대 주택
        if (uri.endsWith("rentals"))
            fetchAndCacheData(Constants.API_RENTAL_URL, request, response);
            // 공공 분양 주택
        else if(uri.endsWith("sales"))
            fetchAndCacheData(Constants.API_SALES_URL, request, response);
        else{
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(Constants.MAPPER.writeValueAsString(ApiResponse.error("Invalid URI")));
        }
    }

    /*
    *
    * BufferedOutputStream -> 파일을 처리할 때 유용
    * */
    // OpenAPI 데이터 가져오기
    private void fetchAndCacheData(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder builder = new StringBuilder();
        RequestDto requestDto = null;
        String str = reader.readLine();
        if(str != null) {
            do {
                builder.append(str);
            } while ((str = reader.readLine()) != null);
            requestDto = Constants.MAPPER.readValue(builder.toString(), RequestDto.class);
        }
        logger.info("requestDto: {}", requestDto);

        // TODO: url 컨버팅?
        URL apiUrl = new URL(getBaseUrl(url, requestDto));
        HttpURLConnection conn = null;
        ApiResponse<ResponseDto> resp;

        try {
            conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 연결 타임아웃 (5초)
            conn.setReadTimeout(15000); // 읽기 타임아웃 (5초)
            conn.setRequestProperty("Content-Type", "application/json");
            int responseCode = conn.getResponseCode();
            // 정상적으로 데이터를 조회
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                builder.setLength(0);
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    builder.append(inputLine);
                }
                // body 이후의 값만 넘기도록 변경
                JsonNode rootNode = Constants.MAPPER.readTree(builder.toString());

                JsonNode headerNode = rootNode.path("response").path("header");
                String resultCode = headerNode.path("resultCode").asText();
                String resultMsg = headerNode.path("resultMsg").asText();
                // 정상적인 경우에는 데이터만 넘김
                if (resultCode.equals("00")) {
                    JsonNode bodyNode = rootNode.path("response").path("body");
                    JsonNode itemNode = bodyNode.path("item");
                    String totalCount = bodyNode.path("totalCount").asText();
                    String numOfRows = bodyNode.path("numOfRows").asText();
                    String pageNo = bodyNode.path("pageNo").asText();
                    resp = ApiResponse.ok(new ResponseDto.Builder().totalCount(totalCount).numOfRows(numOfRows).pageNo(pageNo).data(itemNode.toString()).build());
                } else {
                    resp = ApiResponse.error(resultMsg);
                }
                // TODO: 전송하기 전에 캐싱 하는 기능 추가해야함
            } else {
                resp = ApiResponse.error("Http error : " + conn.getResponseMessage());
            }
            // json으로 데이터 전송
            sendResponse(response, resp);

        } catch (Exception e) {
            logger.error("api call failed : (url : {}, cause : {})", url, e.getMessage());
            sendResponse(response, ApiResponse.error("server error"));
        }
        finally {
            reader.close();
            if (conn != null)
                conn.disconnect();
        }
    }

    private String getBaseUrl(String url, RequestDto request) {
        StringBuilder builder = new StringBuilder(Constants.API_BASE_URL + url + "?serviceKey=" + apiKey);
        if (request != null) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("pageNo", request.getPageNo());
//            params.put("brtcCode", request.getBrtcCode());
//            params.put("signguCode", request.getSignguCode());
//            params.put("yearMtBegin", request.getYearMtBegin());
//            params.put("yearMtEnd", request.getYearMtEnd());

            String queryString = params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null) // null 값 제외
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&")); // "&"로 연결

            if (!queryString.isEmpty())
                builder.append("&").append(queryString);
        }

        return builder.toString();
    }

    private void sendResponse(HttpServletResponse response, ApiResponse<ResponseDto> resp) throws IOException {
        // 데이터를 클라이언트로 전송
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
        String respJson = Constants.MAPPER.writeValueAsString(resp);
        writer.write(respJson);
        writer.flush();
        writer.close();
    }
}
