package com.emlogis.common.facade.workflow.helper;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Site;

import javax.ejb.*;
import java.util.Locale;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestLocaleHelper {

    @EJB
    private ServiceHelper serviceHelper;

    public Locale identifyEmployeeLocale(Employee employee) {
        String lang = "en";
        String country = "US";
        Site employeeSite = serviceHelper.site(employee.getHomeTeam());
        if (employee.getLanguage() != null) {
            lang = employee.getLanguage();
        } else if (employeeSite.getLanguage() != null) {
            lang = employeeSite.getLanguage();
        }
        if (employee.getCountry() != null) {
            country = employee.getCountry();
        } else if (employeeSite.getCountry() != null) {
            country = employeeSite.getCountry();
        }
        if (country == null && lang == null) {
            return new Locale("en", "US");
        } else if (country == null && lang != null) {
            return new Locale(lang);
        }
        return new Locale(lang, country);
    }
}
