package com.emlogis.common.facade.workflow.dashboard.details.annotations;

import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Created by user on 25.08.15.
 */
public class RequestDetailsBuilderQualifierImpl
        extends AnnotationLiteral<RequestDetailsBuilderQualifier>
        implements RequestDetailsBuilderQualifier {

  //  private static final long serialVersionUID = 6471734834552932687L;

    private final String value;

    public RequestDetailsBuilderQualifierImpl(
            final WorkflowRequestTypeDict requestType,
            final WorkflowRoleDict role
    ) {
        this.value = requestType.name()+"_"+role.name()+"_DETAILS";
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
