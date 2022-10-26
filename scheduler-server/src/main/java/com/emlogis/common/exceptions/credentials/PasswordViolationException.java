package com.emlogis.common.exceptions.credentials;

public class PasswordViolationException extends PendingPasswordChangeException {

    public PasswordViolationException() {}

    public PasswordViolationException(String message) {
        super(message);
    }

    public PasswordViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordViolationException(Throwable cause) {
        super(cause);
    }

    public PasswordViolationException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
