package com.netty.openapi.dto;

public class ResponseDto {
    private String totalCount;
    private String numOfRows;
    private String pageNo;
    private String data;

    public ResponseDto() {}

    private ResponseDto(Builder builder) {
        this.totalCount = builder.totalCount;
        this.numOfRows = builder.numOfRows;
        this.pageNo = builder.pageNo;
        this.data = builder.data;
    }

    public static class Builder {
        private String totalCount;
        private String numOfRows;
        private String pageNo;
        private String data;

        public Builder totalCount(String totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder numOfRows(String numOfRows) {
            this.numOfRows = numOfRows;
            return this;
        }

        public Builder pageNo(String pageNo) {
            this.pageNo = pageNo;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public ResponseDto build() {
            return new ResponseDto(this);
        }
    }

    public String getTotalCount() {
        return totalCount;
    }

    public String getNumOfRows() {
        return numOfRows;
    }

    public String getPageNo() {
        return pageNo;
    }

    public String getData() {
        return data;
    }
}
