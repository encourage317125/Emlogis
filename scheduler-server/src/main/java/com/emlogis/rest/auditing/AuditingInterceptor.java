package com.emlogis.rest.auditing;

import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.ESClientService;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.core.Response;


@Audited
@Interceptor
public class AuditingInterceptor {

	private final Logger logger = LoggerFactory.getLogger(AuditingInterceptor.class);

	@Inject SessionService sessionSvc;

	@Inject ESClientService esClientService;


	public AuditingInterceptor() {
		super();
	}

	@AroundInvoke
	public Object auditInvocation(InvocationContext ctx) throws Exception {
		String resourceCname = ctx.getMethod().getDeclaringClass().getSimpleName();
		String resourceMethod = ctx.getMethod().getName();
		logger.debug("Entering method: " + resourceCname  + "." + resourceMethod + "() with " + ctx.getParameters().length + " parameters ");

		// get the AuditRecord prepared by the SecurityFilter and add information to it
		AuditRecord ar = sessionSvc.getAuditRecord();
		if (ar == null) {
			// create a new one in case we failed to get the 
			ar = new AuditRecord();
			ar.setCategory(LogRecordCategory.Application);		// done by security filter via annotation
			ar.setResourceClass(resourceCname);				// done by security filter
			ar.setResourceMethod(resourceMethod);				// done by security filter
		}

		// get the target REST resource so that we can get its input params, invoke it and get result
		Object target = ctx.getTarget();

		// do some marshalling on input parameters as ES 
		// would be cool to get parameter names ... however this info is available only when code is compiled in debug mode

		// for ES to correctly identify the type of input parameters and return values, types must be cosnsistent accross calls
		// thus we build a unique name for each parameter and method return based on resource/method/param index name
		// otherwise, should we have fixed parameter names, the data type would change on each method call and ES wouln't be able to index it.
		String paramPrefix = resourceCname + "_" + resourceMethod + "_";
		ParametersLogging paramsLogging = ar.getParamsLogging();
		if (paramsLogging == ParametersLogging.All || paramsLogging == ParametersLogging.InputOnly) {
			Object inputParams[] = ctx.getParameters();
			int i = 0;
			for (Object in : inputParams) {
				ar.setInputParam(paramPrefix + i++, in);
			}
			if (i == 0) {		// clear input paramater fields if no param. (just to save some resources)
				ar.getInputParams().clear();
			}
		} else {
			ar.setInputParam("hidden param", "");        	
		}

		StopWatch watch = new StopWatch();
		watch.start();

		try {
			// invoke resource method
			Object result = ctx.proceed();
			watch.stop();
			ar.setCallDuration(watch.getTime());

			// and process result for audit log
			// log call. This MUST NOT FAIL otherwise we would screw up the client call
			try {
				if (result != null) {
					if (result instanceof Response) {
						Response resp = (Response)result;
						ar.setHTTPReturnCode(Response.Status.fromStatusCode(resp.getStatus()));
					}
					else {
						// assume response code is OK ??? this may be incorrect ?
						ar.setHTTPReturnCode(Response.Status.OK);
					}
					if (paramsLogging == ParametersLogging.All || paramsLogging == ParametersLogging.OutputOnly) {
						ar.setOutputParam(paramPrefix + "output", result); 
					}        
					else {
						ar.setOutputParam("hidden param", "");        	
					}
					//logger.debug("Exiting method with result: " + String.valueOf(result));
				}
				else {
					// what HTTP code is returned ? so that we can put it in audit log
					logger.debug("Exiting method with result=NULL");    			
				}
				esClientService.indexAuditRecord( ar.getTenantId(), ar);    		
			}
			catch (Throwable t) {
				// ignore exception and 
				t.printStackTrace();
				logger.error("indexAuditRecord: " + t.toString(), t);
			}
			return result;
		}
		catch (Throwable t) {
			if (!watch.isStopped()) { watch.stop(); }
			// in case of exception, add as much information as possible into record, but don't log it,
			// as RESTExceptionHandler will log it
			t.printStackTrace();
			ar.setCallDuration( watch.getTime());
			ar.setException(t.toString());
			ar.setStackTrace(t.getStackTrace());
			logger.error("Exiting method with Exception: " + t.toString(),t);
			throw t;
		}
		finally {   		
		}

	}

}
