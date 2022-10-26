package com.emlogis.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrii Mozharovskyi on 7/30/15.
 */
public abstract class ReportDto<T extends ReportDataDto> implements Serializable {
    protected String reportDate;
    protected String tenantName;
    protected List<T> data;

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public List<T> getData() {
        if(data == null){
            data = new ArrayList<>();
        }
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
