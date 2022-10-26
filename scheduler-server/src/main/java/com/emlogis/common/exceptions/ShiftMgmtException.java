package com.emlogis.common.exceptions;

import javax.ejb.ApplicationException;

import java.util.HashMap;
import java.util.Map;

@ApplicationException(rollback = true)
public class ShiftMgmtException extends RuntimeException {
	
	public enum ShiftMgmtErrorCode {
		UnexpectedError,
		NoEngineAvailable ,
		EngineExecutionTimeOut,
		EngineExecutionError,
		ShiftDoesNotExist,
		EmployeeDoesntQualify,
		ShiftNoLongerAvailableForAssignment
	}

    private ShiftMgmtErrorCode	errorCode;
    private Map<String, Object> paramMap = new HashMap();


    public ShiftMgmtException(Throwable cause) {
        super(cause);
        errorCode = ShiftMgmtErrorCode.UnexpectedError;
    }

    public ShiftMgmtException(ShiftMgmtErrorCode errorCode, String message, Map<String, Object> paramMap) {
        super(message);
        this.errorCode = errorCode;
        this.paramMap = paramMap;
    }

    public ShiftMgmtException(Throwable cause, ShiftMgmtErrorCode errorCode, String message, Map<String, Object> paramMap) {
        super(message, cause);
        this.errorCode = errorCode;
        this.paramMap = paramMap;
    }

    public ShiftMgmtErrorCode getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ShiftMgmtErrorCode errorCode) {
		this.errorCode = errorCode;
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
