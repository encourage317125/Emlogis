package com.emlogis.common.exceptions;

import java.util.Map;

public class RegexValidationException extends ValidationException {

    public RegexValidationException() {}

    public RegexValidationException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RegexValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegexValidationException(String message) {
        super(message);
    }

    public RegexValidationException(Throwable cause) {
        super(cause);
    }

    public RegexValidationException(String message, Map<String, Object> paramMap) {
        super(message, paramMap);
    }

    public RegexValidationException(String message, Throwable cause, Map<String, Object> paramMap) {
        super(message, cause, paramMap);
    }

    public RegexValidationException(Throwable cause, Map<String, Object> paramMap) {
        super(cause, paramMap);
    }
}
