package com.emlogis.common.exceptions.credentials;

public class ExpiredPasswordException extends EmLogisCredentialsException {

    public ExpiredPasswordException() {}

    public ExpiredPasswordException(String message) {
        super(message);
    }

    public ExpiredPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExpiredPasswordException(Throwable cause) {
        super(cause);
    }

    public ExpiredPasswordException(String message, Throwable cause, boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
