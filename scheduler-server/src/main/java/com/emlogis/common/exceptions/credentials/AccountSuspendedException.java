package com.emlogis.common.exceptions.credentials;

public class AccountSuspendedException extends EmLogisCredentialsException {

    public AccountSuspendedException() {}

    public AccountSuspendedException(String message) {
        super(message);
    }

    public AccountSuspendedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountSuspendedException(Throwable cause) {
        super(cause);
    }

    public AccountSuspendedException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
