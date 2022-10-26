package com.emlogis.common.facade.workflow.description;

import com.emlogis.model.workflow.entities.WorkflowRequest;

import java.util.Locale;

/**
 * Created by user on 20.08.15.
 */
public interface DescriptionBuilder {

    String build(WorkflowRequest request, org.joda.time.DateTimeZone dtz, Locale locale);
}
