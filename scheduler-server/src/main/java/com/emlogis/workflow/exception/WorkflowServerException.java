package com.emlogis.workflow.exception;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.emlogis.workflow.WflUtil.currentDate;

/**
 * Created by alexborlis on 22.01.15.
 */
public class WorkflowServerException extends RuntimeException {

    private String code;
    private ExceptionType type;
    private Map<Date, String> messages;

    public WorkflowServerException() {
    }

    public WorkflowServerException(ExceptionCode exCode, String message) {
        super(message);
        this.messages = new ConcurrentHashMap<>();
        messages.put(currentDate(), message);
        this.code = exCode.getCode();
        this.type = exCode.getType();
    }

    public WorkflowServerException(ExceptionCode exCode, String message, Throwable cause) {
        super(message, cause);
        this.messages = new ConcurrentHashMap<>();
        messages.put(currentDate(), message);
        this.code = exCode.getCode();
        this.type = exCode.getType();
    }

    public WorkflowServerException(ExceptionCode exCode, Throwable cause) {
        super(cause);
        this.messages = new ConcurrentHashMap<>();
        messages.put(currentDate(), cause.getMessage());
        this.code = exCode.getCode();
        this.type = exCode.getType();
    }

    public WorkflowServerException(ExceptionCode exCode, String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.messages = new ConcurrentHashMap<>();
        messages.put(currentDate(), cause.getMessage());
        messages.put(currentDate(), message);
        this.code = exCode.getCode();
        this.type = exCode.getType();
    }

    public String getCode() {
        return code;
    }

    public ExceptionType getType() {
        return type;
    }

    public Map<Date, String> getMessages() {
        return messages;
    }
}
