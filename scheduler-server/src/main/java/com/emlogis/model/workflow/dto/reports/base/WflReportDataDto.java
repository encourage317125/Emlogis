package com.emlogis.model.workflow.dto.reports.base;

import com.emlogis.model.dto.ReportDataDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflReportDataDto extends ReportDataDto implements Serializable {

    private Long requestDate;
    private String teamName;

    public WflReportDataDto() {
    }

    public Long getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Long requestDate) {
        this.requestDate = requestDate;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
