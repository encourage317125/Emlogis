package com.emlogis.model.workflow.dto.process.response;

import java.io.Serializable;

/**
 * Created by user on 21.07.15.
 */
public class ErrorSubmitResultDto extends SubmitResultDto implements Serializable {

    private String identifier;

    private String message;

    private String shiftId;

    public ErrorSubmitResultDto() {
    }

    public ErrorSubmitResultDto(
            Boolean isSuccess,
            String message,
            String shiftId,
            String identifier
    ) {
        super(isSuccess);
        this.message = message;
        this.shiftId = shiftId;
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
