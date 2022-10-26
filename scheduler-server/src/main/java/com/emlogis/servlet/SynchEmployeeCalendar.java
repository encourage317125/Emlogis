package com.emlogis.servlet;

import com.emlogis.common.EmployeeCalendarUtils;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Employee;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/emlogis/calendar/*")
public class SynchEmployeeCalendar extends HttpServlet {

    @Inject
    private EmployeeFacade employeeFacade;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        PrimaryKey employeePrimaryKey;

        String[] pathElements = request.getRequestURI().split("/");
        String hash = pathElements[pathElements.length - 1];
        if (!StringUtils.equalsIgnoreCase("calendar", hash)) {
            Employee employee = employeeFacade.findEmployeeByHash(hash);
            if (employee != null) {
                employeePrimaryKey = employee.getPrimaryKey();
            } else {
                throw new com.emlogis.common.exceptions.ValidationException("Incorrect params");
            }
        } else {
            throw new com.emlogis.common.exceptions.ValidationException("Not enough params");
        }

        org.joda.time.DateTime dateTime = new org.joda.time.DateTime();
        dateTime = dateTime.minusMonths(3);

        List<EmployeeCalendarUtils.CalendarEvent> allCurrentVEvents =
                employeeFacade.employeeICalendarInfo(employeePrimaryKey, dateTime.getMillis());

        try {
            Calendar iCal = EmployeeCalendarUtils.createICalendar(allCurrentVEvents);

            if (iCal != null) {
                response.setContentType("text/iCal");
                final CalendarOutputter output = new CalendarOutputter();
                output.setValidating(false);
                output.output(iCal, response.getOutputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) {
        try {
            doPost(request, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
