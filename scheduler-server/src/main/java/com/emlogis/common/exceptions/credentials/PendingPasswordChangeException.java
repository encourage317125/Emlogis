package com.emlogis.common.exceptions.credentials;

public class PendingPasswordChangeException extends EmLogisCredentialsException {

    public PendingPasswordChangeException() {}

    public PendingPasswordChangeException(String message) {
        super(message);
    }

    public PendingPasswordChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PendingPasswordChangeException(Throwable cause) {
        super(cause);
    }

    public PendingPasswordChangeException(String message, Throwable cause, boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
