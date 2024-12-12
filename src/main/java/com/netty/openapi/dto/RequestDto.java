package com.netty.openapi.dto;

public class RequestDto {
    private String reqNo;
    private String reqUrl;
    private String pageNo;

    public RequestDto() {}

    public RequestDto(String reqNo) {
        this.reqNo = reqNo;
    }

    public RequestDto(String reqNo, String reqUrl) {
        this.reqNo = reqNo;
        this.reqUrl = reqUrl;
    }

    public RequestDto(String reqNo, String reqUrl, String pageNo) {
        this.reqNo = reqNo;
        this.reqUrl = reqUrl;
        this.pageNo = pageNo;
    }

    public String getReqNo() {
        return reqNo;
    }
    public String getReqUrl() {
        return reqUrl;
    }

    public String getPageNo() {
        return pageNo;
    }

    @Override
    public String toString() {
        return "{" +
                "reqNo='" + reqNo + '\'' +
                ", reqUrl='" + reqUrl + '\'' +
                ", pageNo='" + pageNo + '\'' +
                '}';
    }
}
