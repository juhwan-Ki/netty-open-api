package com.netty.openapi.dto;

public class ResponseDto {
    private final String status;
    private final String code;
    private final String message;
    private final Data data;

    private ResponseDto(Builder builder) {
        this.status = builder.status;
        this.code = builder.code;
        this.message = builder.message;
        this.data = builder.data;
    }

    public Data getData() {
        return data;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public static class Builder {
        private String status;
        private String code;
        private String message;
        private Data data;

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Data data) {
            this.data = data;
            return this;
        }

        public ResponseDto build() {
            return new ResponseDto(this);
        }
    }
}
