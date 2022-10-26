package com.emlogis.engine.domain;

public class ScheduleReport {

    private String completionReport; // engine execution report data for providing feedback to user.
    private String hardScoreDetails;
    private String softScoreDetails;

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
