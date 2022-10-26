package com.emlogis.workflow.exception;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.emlogis.workflow.WflUtil.currentDate;

/**
 * Created by alexborlis on 22.01.15.
 */
public class WorkflowClientException extends RuntimeException {

    private String code;
    private ExceptionType type;
    private Map<Date, String> messages;

    public WorkflowClientException(WorkflowServerException serverException, String message) {
        this.messages = new ConcurrentHashMap<>();
        messages.putAll(serverException.getMessages());
        messages.put(currentDate(), message);
        this.code = serverException.getCode();
        this.type = serverException.getType();
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

    public String messagesStr() {
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<Date, String>> iterator = messages.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Date, String> entry = iterator.next();
            builder.append("date: " + entry.getKey().toString() + ", cause: " + entry.getValue() + "\n");
        }
        return builder.toString();
    }
}
