package com.emlogis.workflow.controller.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 02.10.15.
 */
public class WipSubmitDto extends RequestSubmitDto implements Serializable {

    private String submitterShiftId;
    private List<String> recipientIds;

    public WipSubmitDto() {
    }

    public String getSubmitterShiftId() {
        return submitterShiftId;
    }

    public void setSubmitterShiftId(String submitterShiftId) {
        this.submitterShiftId = submitterShiftId;
    }

    public List<String> getRecipientIds() {
        if(recipientIds == null) {
            recipientIds = new ArrayList<>();
        }
        return recipientIds;
    }

    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds;
    }
}
