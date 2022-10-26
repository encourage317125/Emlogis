package com.emlogis.common.exceptions.credentials;

public class ForceChangeOnFirstLogonException extends PendingPasswordChangeException {

    public ForceChangeOnFirstLogonException() {}

    public ForceChangeOnFirstLogonException(String message) {
        super(message);
    }

    public ForceChangeOnFirstLogonException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForceChangeOnFirstLogonException(Throwable cause) {
        super(cause);
    }

    public ForceChangeOnFirstLogonException(String message, Throwable cause, boolean enableSuppression,
                                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
