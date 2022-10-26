package com.emlogis.common.facade.workflow.notification.annotations;

import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Created by user on 25.08.15.
 */
public class RequestNotificationBuilderQualifierImpl
        extends AnnotationLiteral<RequestNotificationBuilderQualifier>
        implements RequestNotificationBuilderQualifier {

  //  private static final long serialVersionUID = 6471734834552932687L;

    private final String value;

    public RequestNotificationBuilderQualifierImpl(
            final WorkflowRequestTypeDict requestType,
            final WorkflowRoleDict role
    ) {
        this.value = requestType.name()+"_"+role.name()+"_NOTIFICATION";
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
