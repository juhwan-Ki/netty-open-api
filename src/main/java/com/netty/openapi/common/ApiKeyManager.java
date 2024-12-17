package com.netty.openapi.common;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiKeyManager {
    private static String apiKey;

    public static String getApiKey() throws IOException {
        if (apiKey == null) {
            Properties properties = new Properties();
            InputStream in = null;
            try {
                in = ApiKeyManager.class.getClassLoader().getResourceAsStream("api-key.properties");
                apiKey = properties.getProperty("api-key");
                if (in == null) {
                    throw new IOException("api-key.properties file not found");
                }

                properties.load(in);
                apiKey = properties.getProperty("api-key");

                if (apiKey == null) {
                    throw new IOException("API Key not found in properties file");
                }
            } catch (IOException e) {
                throw new IOException("Failed to load API key from properties file", e);
            } finally {
                if (in != null) try { in.close(); } catch (IOException e) { }

            }
        }
        return apiKey;
    }
}
