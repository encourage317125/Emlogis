package com.emlogis.model.workflow.dto.reports.requests;

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
public class OpenShiftWflReportDataDto extends WflReportDataDto implements Serializable {

   // @XmlElement(name = "employeeName")
    private String employeeName;
    //@XmlElement(name = "requests")
    private List<OpenShiftRequestDataDto> requests;

    public OpenShiftWflReportDataDto() {
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public List<OpenShiftRequestDataDto> getRequests() {
        if(requests == null){
            requests = new ArrayList<>();
        }
        return requests;
    }

    public void setRequests(List<OpenShiftRequestDataDto> requests) {
        this.requests = requests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenShiftWflReportDataDto that = (OpenShiftWflReportDataDto) o;

        if (employeeName != null ? !employeeName.equals(that.employeeName) : that.employeeName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return employeeName != null ? employeeName.hashCode() : 0;
    }
}
