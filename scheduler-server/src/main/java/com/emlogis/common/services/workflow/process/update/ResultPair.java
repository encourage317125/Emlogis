package com.emlogis.common.services.workflow.process.update;

/**
 * Created by user on 11.06.15.
 */
public class ResultPair {

    private Boolean result;
    private String message;

    public ResultPair(Boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
