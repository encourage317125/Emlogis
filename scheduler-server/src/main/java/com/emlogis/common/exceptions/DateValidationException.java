package com.emlogis.common.exceptions;

import java.util.Map;

public class DateValidationException extends ValidationException {

    public DateValidationException() {}

    public DateValidationException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DateValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DateValidationException(String message) {
        super(message);
    }

    public DateValidationException(Throwable cause) {
        super(cause);
    }

    public DateValidationException(String message, Map<String, Object> paramMap) {
        super(message, paramMap);
    }

    public DateValidationException(String message, Throwable cause, Map<String, Object> paramMap) {
        super(message, cause, paramMap);
    }

    public DateValidationException(Throwable cause, Map<String, Object> paramMap) {
        super(cause, paramMap);
    }
}
