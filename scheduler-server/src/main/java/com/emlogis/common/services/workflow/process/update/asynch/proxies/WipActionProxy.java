package com.emlogis.common.services.workflow.process.update.asynch.proxies;

import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.common.WorkflowActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.schedule.QualificationRequestSummary;

import javax.ejb.*;

/**
 * Created by user on 11.06.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WipActionProxy extends WorkflowActionProxy {

    @EJB
    private ScheduleFacade scheduleFacade;


    public ResultPair execute(
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            PrimaryKey wipEmployeePk,
            Boolean force,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk) {
        try {
            QualificationRequestSummary result =
                    scheduleFacade.manualShiftWIP(schedulePk, shiftPk, wipEmployeePk, force,
                            requestPk.getId(), managerPk.getId(), reason);
            return new ResultPair(result.isSuccess(), result.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultPair(false, e.getMessage());
        }
    }


}
