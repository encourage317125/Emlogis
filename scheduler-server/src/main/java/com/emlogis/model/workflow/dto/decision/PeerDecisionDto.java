package com.emlogis.model.workflow.dto.decision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 02.06.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("PEER")
public class PeerDecisionDto extends WorkflowDecisionDto {

    @JsonProperty(value = "shiftIdList", required = false)
    private List<String> shiftIdList;

    public PeerDecisionDto() {
    }

    public List<String> getShiftIdList() {
        if(shiftIdList == null) {
            shiftIdList = new ArrayList<>();
        }
        return shiftIdList;
    }

    public void setShiftIdList(List<String> shiftIdList) {
        this.shiftIdList = shiftIdList;
    }
}
