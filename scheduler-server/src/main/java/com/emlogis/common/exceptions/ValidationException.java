package com.emlogis.common.exceptions;

import javax.ejb.ApplicationException;
import java.util.HashMap;
import java.util.Map;

@ApplicationException(rollback = true)
public class ValidationException extends RuntimeException {

    private Map<String, Object> paramMap;

	public ValidationException() {
		super();
	}

	public ValidationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

    public ValidationException(String message, Map<String, Object> paramMap) {
        super(message);
        this.paramMap = paramMap;
    }

    public ValidationException(String message, Throwable cause, Map<String, Object> paramMap) {
        super(message, cause);
        this.paramMap = paramMap;
    }

    public ValidationException(Throwable cause, Map<String, Object> paramMap) {
        super(cause);
        this.paramMap = paramMap;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public void putParamValue(String param, Object value) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        paramMap.put(param, value);
    }
}
