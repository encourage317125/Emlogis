package com.emlogis.workflow.controller.dto;

import java.io.Serializable;

/**
 * Created by user on 02.10.15.
 */
public class AuthWipSubmitDto extends WipSubmitDto implements Serializable {

    private String tenantId;
    private String login;
    private String password;


    public AuthWipSubmitDto() {
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
