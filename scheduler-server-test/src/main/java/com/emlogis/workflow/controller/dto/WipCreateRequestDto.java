package com.emlogis.workflow.controller.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 01.10.15.
 */
public class WipCreateRequestDto implements Serializable {

    private Boolean isConcurrent;
    private Integer callTimeOut;
    private String url;
    private List<AuthWipSubmitDto> requests;

    public WipCreateRequestDto() {
    }

    public Boolean getIsConcurrent() {
        return isConcurrent;
    }

    public void setIsConcurrent(Boolean isConcurrent) {
        this.isConcurrent = isConcurrent;
    }

    public Integer getCallTimeOut() {
        return callTimeOut;
    }

    public void setCallTimeOut(Integer callTimeOut) {
        this.callTimeOut = callTimeOut;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<AuthWipSubmitDto> getRequests() {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        return requests;
    }

    public void setRequests(List<AuthWipSubmitDto> requests) {
        this.requests = requests;
    }
}
