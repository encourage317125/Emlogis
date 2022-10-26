package com.emlogis.common.facade.workflow.notification.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by user on 25.08.15.
 */
@Qualifier
@Target({TYPE})
@Retention(RUNTIME)
@Documented
public @interface RequestNotificationBuilderQualifier {

    /**
     * Qualification {@link String} value
     *
     * @return
     */
    String value();
}
