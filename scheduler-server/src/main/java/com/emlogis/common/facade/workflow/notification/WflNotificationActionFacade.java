package com.emlogis.common.facade.workflow.notification;

import com.amazonaws.util.Base64;
import com.emlogis.common.facade.workflow.helper.ServiceHelper;
import com.emlogis.common.facade.workflow.process.action.RequestActionFacade;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.api.crypt.ICryptoService;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowClientException;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.ejb.*;
import java.io.UnsupportedEncodingException;

import static com.emlogis.workflow.WflUtil.UTF_8;

//import com.emlogis.workflow.context.WorkflowEngineContext;

/**
 * Created by alex on 3/19/15.
 */
@Stateless
@Local(value = IWflNotificationActionFacade.class)
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class WflNotificationActionFacade implements IWflNotificationActionFacade {

    private final static Logger logger = Logger.getLogger(WflNotificationActionFacade.class);

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private WorkflowRequestService processInstanceService;

    @EJB
    private ICryptoService cryptoService;

    @EJB
    private ServiceHelper serviceHelper;

    @EJB
    private RequestActionFacade acdtionFcd;

    @Override
    public String processRequest(
            String code,
            String userAccount,
            String tenant,
            String decision,
            WorkflowRoleDict role
    ) throws WorkflowClientException {
        try {
            String userAccountId = new String(Base64.decode(userAccount.getBytes(UTF_8)));
            UserAccount account = serviceHelper.account(tenant, userAccountId);
            //Employee employee = serviceHelper.account(tenant, userAccountId).getEmployee();
            ProcessRequestDecisionType decisionType = ProcessRequestDecisionType.valueOf(decision);
            WorkflowRequest instance = processInstanceService.findByCode(
                    cryptoService.decrypt(account.getId(), code.getBytes(UTF_8)));
            if (decisionType.equals(ProcessRequestDecisionType.APPROVE)) {
                return acdtionFcd.approve(instance, account, role);
            } else {
                return acdtionFcd.deny(instance, account, role);
            }
        } catch (WorkflowServerException exception) {
            StringBuilder builder = new StringBuilder();
            return builder.
                    append("<!DOCTYPE HTML>\n").
                    append("<html>\n").
                    append("<head>\n").
                    append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").
                    append("<title>Dear USer:" + userAccount + "</title>\n").
                    append("<p>Wrong request error occurred " + exception.getMessage() + " </p>\n").
                    append("<p>So we can not process your request </p>\n").
                    append("</body>\n").
                    append("</html>\n").
                    toString();
        } catch (UnsupportedEncodingException exception) {
            StringBuilder builder = new StringBuilder();
            return builder.
                    append("<!DOCTYPE HTML>\n").
                    append("<html>\n").
                    append("<head>\n").
                    append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").
                    append("<title>Dear USer:" + userAccount + "</title>\n").
                    append("<p>Unsupported Encoding error occurred " + exception.getMessage() + " </p>\n").
                    append("<p>So we can not process your request </p>\n").
                    append("</body>\n").
                    append("</html>\n").
                    toString();
        } catch (Exception exception) {
            StringBuilder builder = new StringBuilder();
            return builder.
                    append("<!DOCTYPE HTML>\n").
                    append("<html>\n").
                    append("<head>\n").
                    append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n").
                    append("<title>Dear USer:" + userAccount + "</title>\n").
                    append("<p>Unrecognized Error occurred " + exception.getMessage() + " </p>\n").
                    append("<p>So we can not process your request </p>\n").
                    append("</body>\n").
                    append("</html>\n").
                    toString();
        }
    }
}
