package com.emlogis.common.facade.workflow.validator;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;

import java.io.Serializable;

/**
 * Created by Alexander on 27.10.2015.
 */
public class SubmitPreValidationResultDto implements Serializable {

    private Employee employee;
    private UserAccount userAccount;
    private Boolean result;
    private String message;

    public SubmitPreValidationResultDto(UserAccount userAccount, Employee employee, boolean result, String message) {
        this.employee = employee;
        this.result = result;
        this.message = message;
        this.userAccount = userAccount;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
