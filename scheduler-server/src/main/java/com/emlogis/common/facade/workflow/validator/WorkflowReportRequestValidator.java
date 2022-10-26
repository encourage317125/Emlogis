package com.emlogis.common.facade.workflow.validator;

import com.emlogis.model.workflow.dto.reports.base.WflReportDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportTeamDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportWorkflowTypeDto;

import java.util.Collection;

/**
 * Created by lucas on 01.06.2015.
 */
public interface WorkflowReportRequestValidator {

    //reports part

    Collection<String> getReportSites();

    Collection<WflReportTeamDto> getReportTeams(String tenantId);

    Collection<WflReportWorkflowTypeDto> getReportTypes();

    WflReportDto report(String type, String site, String[] teams, Long startDate, Long endDate);

}
