package com.netty.openapi.dto;

public class Data {
    private final String totalCount;
    private final String numOfRows;
    private final String pageNo;
    private final String data;

    private Data(Builder builder) {
        this.data = builder.data;
        this.numOfRows = builder.numOfRows;
        this.pageNo = builder.pageNo;
        this.totalCount = builder.totalCount;
    }

    public String getData() {
        return data;
    }

    public String getPageNo() {
        return pageNo;
    }

    public String getNumOfRows() {
        return numOfRows;
    }

    public String getTotalCount() {
        return totalCount;
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

        public Data build() {
            return new Data(this);
        }
    }
}
