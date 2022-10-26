package com.emlogis.common.facade.workflow.validator;

import com.emlogis.common.facade.workflow.reports.WorkflowReportFacade;
import com.emlogis.model.workflow.dto.reports.base.WflReportDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportTeamDto;
import com.emlogis.model.workflow.dto.reports.base.WflReportWorkflowTypeDto;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by lucas on 01.06.2015.
 */
@Local(WorkflowReportRequestValidator.class)
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowReportRequestReportValidatorImpl implements WorkflowReportRequestValidator {

    private final static Logger logger = Logger.getLogger(WorkflowReportRequestReportValidatorImpl.class);

    @EJB
    private WorkflowReportFacade reportFsd;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getReportSites() {
        return reportFsd.findSites();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<WflReportTeamDto> getReportTeams(String tenantId) {
        return reportFsd.findTeams(tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<WflReportWorkflowTypeDto> getReportTypes() {
        return reportFsd.findTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WflReportDto report(
            String type, String site, String[] teams, Long startDate, Long endDate
    ) {
        return reportFsd.report(type, site, Arrays.asList(teams), startDate, endDate);
    }
}
