package com.emlogis.rest.resources.workflow.common;

import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.workflow.exception.WorkflowClientException;
import com.emlogis.workflow.exception.WorkflowServerException;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by alexborlis on 22.01.15.
 */
public final class Utils {

    private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Utils.class);

    private Utils() {
    }

    public static Response responseOk(ResultSet resultSet) {
        try {
            logger.info("response => " + resultSet);
            CustomJsonResponse jsonResponse = new CustomJsonResponse(ResponseStatus.SUCCESS, resultSet.getResult(),
                    Long.valueOf(resultSet.getTotal()));
            Response response = Response.status(Response.Status.OK).entity(jsonResponse).build();
            return response;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new CustomJsonResponse(ResponseStatus.FAIL, "Jackson Json mapping issue")).build();
        }
    }

    public static Response responseOk(Object serviceResult, Long total) {
        try {
            logger.info("response => " + serviceResult);
            Response response = Response.status(Response.Status.OK).entity(
                    new CustomJsonResponse(ResponseStatus.SUCCESS, serviceResult, total)).build();
            return response;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new CustomJsonResponse(ResponseStatus.FAIL, "Jackson Json mapping issue")).build();
        }
    }

    public static Response responseOk(Object serviceResult) {
        try {
            logger.info("response => " + serviceResult);
            Response response = Response.status(Response.Status.OK).entity(
                    new CustomJsonResponse(ResponseStatus.SUCCESS, serviceResult)).build();
            return response;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new CustomJsonResponse(ResponseStatus.FAIL, "Jackson Json mapping issue")).build();
        }
    }

    public static Response responseOk() {
        try {
            return Response.status(Response.Status.OK).entity(
                    new CustomJsonResponse(ResponseStatus.SUCCESS, ResponseStatus.SUCCESS)).build();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new CustomJsonResponse(ResponseStatus.FAIL, "Jackson Json mapping issue")).build();
        }
    }

    public static Response fail(WorkflowClientException exception) {
        //todo:: reimplement with handling error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                new CustomJsonResponse(ResponseStatus.FAIL, "CODE: " + exception.getCode() +
                        " TYPE: " + exception.getType().name() + " trace: \n" + exception.messagesStr())).build();
    }

    public static Response fail(WorkflowServerException exception) {
        //todo:: reimplement with handling error
        StringBuilder exBuilder = new StringBuilder();
        Iterator<Map.Entry<Date, String>> iterator = exception.getMessages().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Date, String> entry = iterator.next();
            exBuilder.append(entry.getKey().toString() + " : " + entry.getValue());
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                new CustomJsonResponse(ResponseStatus.FAIL, "CODE: " + exception.getCode() +
                        " TYPE: " + exception.getType().name() + " trace: \n" + exBuilder.toString())).build();
    }

}
