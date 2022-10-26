package com.emlogis.common.facade.workflow.description.annotations;

import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Created by user on 25.08.15.
 */
public class RequestDescriptionBuilderQualifierImpl extends AnnotationLiteral<RequestDescriptionBuilderQualifier>
        implements RequestDescriptionBuilderQualifier {

    private final String value;

    public RequestDescriptionBuilderQualifierImpl(
            final WorkflowRequestTypeDict requestType,
            final WorkflowRoleDict role
    ) {
        this.value = requestType.name()+"_"+role.name()+"_DESCRIPTION";
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
