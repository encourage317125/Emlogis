package com.emlogis.model.workflow.dto.process.request.submit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 28.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("OPEN_SHIFT_REQUEST")
public class OpenShiftSubmitDto extends SubmitDto implements Serializable {

    @JsonProperty(value = "openShifts", required = true)
    private List<OpenShiftSubmitItemDto> openShifts;

    public OpenShiftSubmitDto() {
    }

    public List<OpenShiftSubmitItemDto> getOpenShifts() {
        if (openShifts == null) {
            openShifts = new ArrayList<>();
        }
        return openShifts;
    }

    public void setOpenShifts(List<OpenShiftSubmitItemDto> openShifts) {
        this.openShifts = openShifts;
    }
}
