package com.emlogis.model.workflow.dto.reports.requests;

import com.emlogis.model.workflow.dto.reports.base.WflReportActorDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportBaseActorDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportDataDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 2/27/15.
 */
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkInPlaceWflReportDataDto extends WflReportDataDto implements Serializable {

  //  @XmlElement(name = "originator")
    private WflReportActorDto originator;

    //@XmlElement(name = "recipients")
    private List<WflReportBaseActorDto> recipients;

    public WorkInPlaceWflReportDataDto() {
    }

    public WflReportActorDto getOriginator() {
        return originator;
    }

    public void setOriginator(WflReportActorDto originator) {
        this.originator = originator;
    }

    public List<WflReportBaseActorDto> getRecipients() {
        if(recipients == null){
            recipients = new ArrayList<>();
        }
        return recipients;
    }

    public void setRecipients(List<WflReportBaseActorDto> recipients) {
        this.recipients = recipients;
    }
}
