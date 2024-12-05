package com.netty.openapi.dto;

public class RequestDto {
    private final String reqNo;
    private final String reqUrl;
    private final String brtcCode;
    private final String signguCode;
    private final String pageNo;
    private final String yearMtBegin;
    private final String yearMtEnd;

    public RequestDto(String reqNo, String reqUrl, String brtcCode, String signguCode, String pageNo, String yearMtBegin, String yearMtEnd) {
        this.reqNo = reqNo;
        this.reqUrl = reqUrl;
        this.brtcCode = brtcCode;
        this.signguCode = signguCode;
        this.pageNo = pageNo;
        this.yearMtBegin = yearMtBegin;
        this.yearMtEnd = yearMtEnd;
    }

    public String getReqNo() {
        return reqNo;
    }
    public String getReqUrl() {
        return reqUrl;
    }

    public String getBrtcCode() {
        return brtcCode;
    }

    public String getSignguCode() {
        return signguCode;
    }

    public String getPageNo() {
        return pageNo;
    }

    public String getYearMtBegin() {
        return yearMtBegin;
    }

    public String getYearMtEnd() {
        return yearMtEnd;
    }
}
