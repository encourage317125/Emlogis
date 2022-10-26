package com.emlogis.common.exceptions;

import java.util.Map;

public class UniqueValidationException extends ValidationException {

    public UniqueValidationException() {}

    public UniqueValidationException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UniqueValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueValidationException(String message) {
        super(message);
    }

    public UniqueValidationException(Throwable cause) {
        super(cause);
    }

    public UniqueValidationException(String message, Map<String, Object> paramMap) {
        super(message, paramMap);
    }

    public UniqueValidationException(String message, Throwable cause, Map<String, Object> paramMap) {
        super(message, cause, paramMap);
    }

    public UniqueValidationException(Throwable cause, Map<String, Object> paramMap) {
        super(cause, paramMap);
    }
}
