package com.emlogis.model.workflow.dto.process.request.submit;

import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 28.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("TIME_OFF_REQUEST")
public class TimeOffSubmitDto extends SubmitDto implements Serializable {

    private List<TimeOffRequestInfoDto> requests;

    public TimeOffSubmitDto() {
    }

    public List<TimeOffRequestInfoDto> getRequests() {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        return requests;
    }

    public void setRequests(List<TimeOffRequestInfoDto> requests) {
        this.requests = requests;
    }
}
