package com.emlogis.workflow.controller.dto;

import java.io.Serializable;

/**
 * Created by user on 02.10.15.
 */
public class ErrorSubmitDto implements Serializable {

    private String message;

    private String shiftId;

    public ErrorSubmitDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }
}
