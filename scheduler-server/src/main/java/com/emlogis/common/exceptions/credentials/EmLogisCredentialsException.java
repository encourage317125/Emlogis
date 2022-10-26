package com.emlogis.common.exceptions.credentials;

public class EmLogisCredentialsException extends Exception {

    public EmLogisCredentialsException() {}

    public EmLogisCredentialsException(String message) {
        super(message);
    }

    public EmLogisCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmLogisCredentialsException(Throwable cause) {
        super(cause);
    }

    public EmLogisCredentialsException(String message, Throwable cause, boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
