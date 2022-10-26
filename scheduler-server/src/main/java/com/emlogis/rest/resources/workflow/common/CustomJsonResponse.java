package com.emlogis.rest.resources.workflow.common;

/**
 * Created by alexborlis on 22.01.15.
 */
public class CustomJsonResponse {

    private ResponseStatus status;
    private Object data;
    private Long total = 0l;

    public CustomJsonResponse(ResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
    }

    public CustomJsonResponse(ResponseStatus status, Object data, Long total) {
        this(status, data);
        this.total = total;
    }

    public CustomJsonResponse() {
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
