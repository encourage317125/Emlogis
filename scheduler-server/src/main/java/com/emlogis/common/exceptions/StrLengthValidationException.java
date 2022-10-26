package com.emlogis.common.exceptions;

import java.util.Map;

public class StrLengthValidationException extends ValidationException {

    public StrLengthValidationException() {}

    public StrLengthValidationException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public StrLengthValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public StrLengthValidationException(String message) {
        super(message);
    }

    public StrLengthValidationException(Throwable cause) {
        super(cause);
    }

    public StrLengthValidationException(String message, Map<String, Object> paramMap) {
        super(message, paramMap);
    }

    public StrLengthValidationException(String message, Throwable cause, Map<String, Object> paramMap) {
        super(message, cause, paramMap);
    }

    public StrLengthValidationException(Throwable cause, Map<String, Object> paramMap) {
        super(cause, paramMap);
    }
}
