package com.emlogis.common.services.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.model.tenant.UserAccount;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class AccountUtilService {

	@EJB
	private TenantService tenantService;

	@EJB
	private EmployeeService employeeService;

//  'all terrain' version to be seen if needed.
//	public DateTimeZone getActualTimeZone(Object o) {
//		if (o instanceof UserAccount) ......
//	}
		
	public DateTimeZone getActualTimeZone(Site site) {
		// return Site Tz if configured
		if (site.getTimeZone() != null) {
			return site.getTimeZone();
		} else {
			// return Tenant Tz otherwise
			return getTenantTimeZone(site);
		}
	}
	
	public DateTimeZone getActualTimeZone(Employee employee) {
		return getActualTimeZone(employee.getUserAccount());
	}
	
	public DateTimeZone getActualTimeZone(UserAccount userAccount) {
		// return employee Tz if account linked to employee
		if (userAccount.getEmployee() != null) {
			return getEmployeeActualTimeZone(userAccount.getEmployee());
		}
		// return account Tz if present
		if (userAccount.getTimeZone() != null) {
			return userAccount.getTimeZone();
		}		
		// return Tenant Tz otherwise
		return getTenantTimeZone(userAccount);
	}

	private DateTimeZone getEmployeeActualTimeZone(Employee employee) {
    	try {
    		// for timezone, get it from employee paremt Site (if configured), from parent Org otherwise
			Site site = employeeService.getEmployeeSite(employee);
			if (site != null && site.getTimeZone() != null) {
				return site.getTimeZone();
			} else {
				return getTenantTimeZone(employee);
			}
		} catch (NoSuchFieldException | IllegalArgumentException
				| IllegalAccessException | NoSuchMethodException
				| InvocationTargetException e) {				
			throw new RuntimeException("Unable to retrieve employee Site for employee: " + employee.getId(),  e);
		}
	}

	private DateTimeZone getTenantTimeZone(BaseEntity entity) {
		return getTenantTimeZone(entity.getTenantId());
	}
	
	private DateTimeZone getTenantTimeZone(String tenantId) {
		Tenant tenant = tenantService.getTenant(tenantId);
		return tenant.getTimeZone();		
	}
	
	public Site getUserSite(UserAccount userAccount) {
		Site retVal = null;
		
		Employee employee = userAccount.getEmployee();
		
		if (employee != null) {
			try {
				retVal = employeeService.getEmployeeSite(employee);
			} catch (NoSuchFieldException | IllegalArgumentException
					| IllegalAccessException | NoSuchMethodException
					| InvocationTargetException e) {
				throw new RuntimeException("Unable to retrieve employee Site for user: " + userAccount.getId(), e);
			}
		}
						
		return retVal;
	}
	
	public Locale getUserLocale(UserAccount userAccount, Site userSite) {
        String lang = "en";
        String country = "US";
        
        if (userAccount.getLanguage() != null) {
            lang = userAccount.getLanguage();
        } else if (userSite != null && userSite.getLanguage() != null) {
            lang = userSite.getLanguage();
        }
        if (userAccount.getCountry() != null) {
            country = userAccount.getCountry();
        } else if (userSite != null && userSite.getCountry() != null) {
            country = userSite.getCountry();
        }
        if (country == null && lang == null) {
            return new Locale("en", "US");
        } else if (country == null && lang != null) {
            return new Locale(lang);
        }
        return new Locale(lang, country);
    }
	
	public String getTimeZoneAdjustedDateString(DateTime appTime, UserAccount userAccount, DateTimeFormatter formatter) {
    	DateTimeZone zone = getActualTimeZone(userAccount);
    	DateTimeFormatter dateFormatter = formatter.withZone(zone);
        return dateFormatter.print(appTime);
    }

}
