package com.emlogis.common.facade.workflow.reports;

import com.emlogis.model.workflow.dto.reports.base.WflReportDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportTeamDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportWorkflowTypeDto;
import com.emlogis.workflow.exception.WorkflowServerException;

import java.util.Collection;
import java.util.List;

/**
 * Created by alex on 2/27/15.
 */
public interface WorkflowReportFacade {
    Collection<String> findSites() throws WorkflowServerException;

    Collection<WflReportTeamDto> findTeams(String tenantId) throws WorkflowServerException;

    Collection<WflReportWorkflowTypeDto> findTypes() throws WorkflowServerException;

    WflReportDto report(String type, String site, List<String> strings, Long startDate, Long endDate) throws WorkflowServerException;
}
