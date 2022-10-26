package com.emlogis.rest.resources;

import com.emlogis.common.Constants;
import com.emlogis.common.ModelUtils;
import com.emlogis.common.exceptions.ShiftMgmtException;
import com.emlogis.common.exceptions.UniqueValidationException;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.exceptions.credentials.EmLogisCredentialsException;
import com.emlogis.rest.auditing.AuditRecord;
import com.emlogis.rest.auditing.LogRecordCategory;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.ESClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.shiro.authc.AuthenticationException;
import org.hibernate.exception.ConstraintViolationException;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Map;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class RestExceptionHandler implements ExceptionMapper<Exception> {
	
	@Inject SessionService sessionService;

	@Inject ESClientService esClientService;

    @Override
    public Response toResponse(Exception exception) {
    	// TODO
    	// add some information in return data for UNAUTHORIZED and FORBIDDEN errors
    	// (see https://emlogis.atlassian.net/wiki/display/AR/APIs)
    	
    	// what we get here can be an EJB exception wrapping the source exception
    	Throwable throwable = exception instanceof EJBException && exception.getCause() != null
                ? exception.getCause() : exception;

        // get the AuditRecord prepared by the SecurityFilter and add information to it
        AuditRecord auditRecord = sessionService.getAuditRecord();
        if (auditRecord == null) {
        	// create a new one in case we failed to get the 
            auditRecord = new AuditRecord();
            auditRecord.setCategory(LogRecordCategory.Application);		// done by security filter via annotation
        }

    	Response.Status status = getHttpStatusCode(throwable);
    	auditRecord.setHTTPReturnCode(status);
    	if (auditRecord.getStackTrace() == null) {
            auditRecord.setException(throwable.toString());
            auditRecord.setStackTrace(throwable.getStackTrace());
    	}

    	// log this exception. This MUST NOT FAIL otherwise we would screw up the client call
    	try {
            esClientService.indexAuditRecord(auditRecord.getTenantId(), auditRecord);
    	} catch (Throwable t) {
    		// ignore exception and 
    	}

        String userMessage = null;

        if (throwable.getCause() instanceof PersistenceException
                && throwable.getCause().getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException violationException =
                    (ConstraintViolationException) throwable.getCause().getCause();
            String exceptionMessage = violationException.getSQLException().getMessage();
            if (exceptionMessage.contains("Cannot delete or update a parent row: a foreign key constraint fails")) {
                userMessage = sessionService.getMessage("entity.constraint.violation");
            }
        }
        if (userMessage == null) {
            userMessage = throwable.getMessage();
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("exception", throwable.getClass().getSimpleName());
        resultMap.put("message", userMessage);
//        resultMap.put("stackTrace", getStackTrace(throwable));
        if (throwable instanceof ValidationException || throwable instanceof ShiftMgmtException) {
            Map<String, Object> paramMap = (throwable instanceof ValidationException ? 
            		((ValidationException) throwable).getParamMap()
            		: ((ShiftMgmtException) throwable).getParamMap());
            if (paramMap != null) {
                resultMap.putAll(paramMap);
            }
            if (throwable instanceof UniqueValidationException) {
                String[] fieldNames = (String[]) paramMap.get(Constants.FIELD_NAMES);
                Class entityClass = (Class) paramMap.get(Constants.VALIDATION_OBJECT_TYPE);
                resultMap.put("entityName", entityClass.getSimpleName());
                String names = ModelUtils.commaSeparatedValues(fieldNames, null);
                resultMap.put("fieldNames", names);
            }
        }
        ObjectMapper objMapper = new ObjectMapper();
        String msg;
		try {
			msg = objMapper.writeValueAsString(resultMap);
		} catch (JsonProcessingException e) {
			msg = exception.getMessage();
		}	
        return Response.status(status).entity(msg).build();
    }
    
    private Response.Status getHttpStatusCode(Throwable throwable) {
        if (throwable instanceof NotFoundException) {
            return Response.Status.NOT_FOUND;
        } else if (throwable instanceof ValidationException || throwable instanceof ShiftMgmtException) {
            return Response.Status.BAD_REQUEST;
        } else if (throwable instanceof EmLogisCredentialsException) {
        	return Response.Status.UNAUTHORIZED;
        } else if (throwable instanceof AuthenticationException) {
        	return Response.Status.UNAUTHORIZED;
        } else if (throwable instanceof AccessControlException) {
        	return Response.Status.FORBIDDEN;
        } else if (throwable instanceof SecurityException) {
        	return Response.Status.FORBIDDEN;
        } else {
        	return Response.Status.INTERNAL_SERVER_ERROR;
        }
    }

    private String getStackTrace(Throwable throwable) {
        String result = "";
        StackTraceElement[] elements = throwable.getStackTrace();
        if (elements != null) {
            for (StackTraceElement element : elements) {
                result += element + "\n";
            }
        }
        return result;
    }

}




