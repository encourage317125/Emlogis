package com.emlogis.common.validation;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.*;
import com.emlogis.common.validation.validators.DateValidator;
import com.emlogis.common.validation.validators.RegexValidator;
import com.emlogis.common.validation.validators.StrLengthValidator;
import com.emlogis.common.validation.validators.UniqueValidatorBean;
import com.emlogis.rest.security.SessionService;

import java.util.Map;

public class ValidationExceptionBuilder {

    private SessionService sessionService;

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public ValidationException build(Map<String, Object> paramMap, Class<? extends Validator> clazz) {
        ValidationException result = null;

        if (clazz == DateValidator.class) {
            result = new DateValidationException(sessionService.getMessage("validation.field.dates.error"));
        } else if (clazz == StrLengthValidator.class) {
            int min = (Integer) paramMap.get(Constants.MIN);
            int max = (Integer) paramMap.get(Constants.MAX);
            String fieldName = (String) paramMap.get(Constants.FIELD_NAME);
            Object value = paramMap.get(Constants.VALUE);
            Boolean passNull = (Boolean) paramMap.get(Constants.PASS_NULL);
            String message;
            if (value == null && !passNull) {
                message = sessionService.getMessage("validation.field.empty.error", fieldName);
            } else {
                message = sessionService.getMessage("validation.field.length.error", fieldName, min, max);
            }
            result = new StrLengthValidationException(message);
        } else if (clazz == RegexValidator.class) {
            String regexp = (String) paramMap.get(Constants.REGEX);
            Boolean caseSensitive = (Boolean) paramMap.get(Constants.CASE_SENSITIVE);
            String fieldName = (String) paramMap.get(Constants.FIELD_NAME);
            result = new RegexValidationException(
                    sessionService.getMessage("validation.field.regexp.error", fieldName, regexp, caseSensitive));
        } else if (clazz == UniqueValidatorBean.class) {
            String[] fieldNames = (String[]) paramMap.get(Constants.FIELD_NAMES);
            Class entityClass = (Class) paramMap.get(Constants.VALIDATION_OBJECT_TYPE);
            String names = ModelUtils.commaSeparatedValues(fieldNames, null);
            result = new UniqueValidationException(sessionService.getMessage("validation.field.unique.error",
                    entityClass.getSimpleName(), names), paramMap);
        }

        return result;
    }

}
