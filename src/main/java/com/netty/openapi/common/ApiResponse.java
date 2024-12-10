package com.netty.openapi.common;

public class ApiResponse <T> {
    private String status;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("success", "", data);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>("success", message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
