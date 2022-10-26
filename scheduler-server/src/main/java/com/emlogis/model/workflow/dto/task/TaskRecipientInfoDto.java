package com.emlogis.model.workflow.dto.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by user on 07.05.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRecipientInfoDto extends TaskRecipientBriefInfoDto implements Serializable {

    private TaskShiftBriefInfoDto recipientShift;

    public TaskRecipientInfoDto() {
    }

    public TaskRecipientInfoDto(
            TaskRecipientBriefInfoDto prnt
    ) {
        super(prnt.getPeerName(), prnt.getPeerId(), prnt.getTeamName(), prnt.getTeamId(), prnt.getComment(),
                prnt.getDateActed(), prnt.getStatus());
    }

    public TaskShiftBriefInfoDto getRecipientShift() {
        return recipientShift;
    }

    public void setRecipientShift(TaskShiftBriefInfoDto recipientShift) {
        this.recipientShift = recipientShift;
    }
}
