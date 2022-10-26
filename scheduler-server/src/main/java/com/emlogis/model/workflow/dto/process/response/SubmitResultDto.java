package com.emlogis.model.workflow.dto.process.response;

import java.io.Serializable;

/**
 * Created by user on 21.07.15.
 */
public class SubmitResultDto implements Serializable {

    private Boolean isSuccess;

    public SubmitResultDto() {
    }

    public SubmitResultDto(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
