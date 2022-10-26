package com.emlogis.model.workflow.dto.reports.base;

import com.emlogis.model.dto.ReportDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by alex on 2/27/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflReportDto extends ReportDto<WflReportDataDto> implements Serializable {

    //private String reportDate;
    private String processType;
    //private String tenantName;
    private String dateRange;
    private String homeTeam;
    //private List<T> data;

    public WflReportDto() {
    }

    public WflReportDto(
            String reportDate, String processType, String tenantName, String dateRange, String homeTeam
    ) {
        this.reportDate = reportDate;
        this.processType = processType;
        this.tenantName = tenantName;
        this.dateRange = dateRange;
        this.homeTeam = homeTeam;
    }

//    public List<T> getData() {
//        if(data == null){
//            data = new ArrayList<>();
//        }
//        return data;
//    }
//
//    public void setData(List<T> data) {
//        this.data = data;
//    }

//    public String getReportDate() {
//        return reportDate;
//    }
//
//    public void setReportDate(String reportDate) {
//        this.reportDate = reportDate;
//    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

//    public String getTenantName() {
//        return tenantName;
//    }
//
//    public void setTenantName(String tenantName) {
//        this.tenantName = tenantName;
//    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }
}
