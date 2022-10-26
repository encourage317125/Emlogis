package com.emlogis.model.schedule;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class ScheduleReport extends BaseEntity {

    @Lob
    private String completionReport; // engine execution report data for providing feedback to user.

    @Column(length = 1024)
    private String hardScoreDetails;				// hard score information as returned by Engine

    @Column(length = 1024)
    private String softScoreDetails;				// hard score information as returned by Engine

    public ScheduleReport() {}

    public ScheduleReport(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public String getCompletionReport() {
        return completionReport;
    }

    public void setCompletionReport(String completionReport) {
        this.completionReport = completionReport;
    }

	public String getHardScoreDetails() {
		return hardScoreDetails;
	}

	public void setHardScoreDetails(String hardScoreDetails) {
		this.hardScoreDetails = hardScoreDetails;
	}

	public String getSoftScoreDetails() {
		return softScoreDetails;
	}

	public void setSoftScoreDetails(String softScoreDetails) {
		this.softScoreDetails = softScoreDetails;
	}
    
}
