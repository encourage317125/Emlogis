package com.emlogis.model.workflow.dto.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by root on 04.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRecipientBriefInfoDto implements Serializable {

    @JsonProperty(value = "peerName", required = true)
    private String peerName;

    @JsonProperty(value = "peerId", required = true)
    private String peerId;

    @JsonProperty(value = "teamName", required = true)
    private String teamName;

    @JsonProperty(value = "teamId", required = true)
    private String teamId;

    @JsonProperty(value = "comment", required = false)
    private String comment;

    @JsonProperty(value = "dateActed", required = false)
    private Long dateActed;

    @JsonProperty(value = "status", required = true)
    private String status;

    public TaskRecipientBriefInfoDto() {
    }

    public TaskRecipientBriefInfoDto(
            String peerName, String peerId, String teamName, String teamId, String comment,
            Long dateActed, String status
    ) {
        this.peerName = peerName;
        this.peerId = peerId;
        this.teamName = teamName;
        this.teamId = teamId;
        this.comment = comment;
        this.dateActed = dateActed;
        this.status = status;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getDateActed() {
        return dateActed;
    }

    public void setDateActed(Long dateActed) {
        this.dateActed = dateActed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
