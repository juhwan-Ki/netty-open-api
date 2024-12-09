package com.netty.openapi.dto;

public class RequestDto {
    private String reqNo;
    private String reqUrl;
//    private String brtcCode;
//    private String signguCode;
//    private String pageNo;
//    private String yearMtBegin;
//    private String yearMtEnd;

    public RequestDto() {}

    public RequestDto(String reqNo) {
        this.reqNo = reqNo;
    }

    public RequestDto(String reqNo, String reqUrl) {
        this.reqNo = reqNo;
        this.reqUrl = reqUrl;
    }
    public String getReqNo() {
        return reqNo;
    }
    public String getReqUrl() {
        return reqUrl;
    }

    @Override
    public String toString() {
        return "{" +
                "reqNo='" + reqNo + '\'' +
                ", reqUrl='" + reqUrl + '\'' +
                '}';
    }

//    public RequestDto(Builder builder) {
//        this.reqNo = builder.reqNo;
//        this.reqUrl = builder.reqUrl;
//        this.brtcCode = builder.brtcCode;
//        this.signguCode = builder.signguCode;
//        this.pageNo = builder.pageNo;
//        this.yearMtBegin = builder.yearMtBegin;
//        this.yearMtEnd = builder.yearMtEnd;
//    }

//    public static class Builder {
//        private String reqNo;
//        private String reqUrl;
//        private String brtcCode;
//        private String signguCode;
//        private String pageNo;
//        private String yearMtBegin;
//        private String yearMtEnd;
//
//        public Builder reqNo(String reqNo) {
//            this.reqNo = reqNo;
//            return this;
//        }
//
//        public Builder reqUrl(String reqUrl) {
//            this.reqUrl = reqUrl;
//            return this;
//        }
//
//        public Builder brtcCode(String brtcCode) {
//            this.brtcCode = brtcCode;
//            return this;
//        }
//
//        public Builder signguCode(String signguCode) {
//            this.signguCode = signguCode;
//            return this;
//        }
//
//        public Builder pageNo(String pageNo) {
//            this.pageNo = pageNo;
//            return this;
//        }
//
//        public Builder yearMtBegin(String yearMtBegin) {
//            this.yearMtBegin = yearMtBegin;
//            return this;
//        }
//
//        public Builder yearMtEnd(String yearMtEnd) {
//            this.yearMtEnd = yearMtEnd;
//            return this;
//        }
//
//        public RequestDto build() {
//            return new RequestDto(this);
//        }
//    }


}
