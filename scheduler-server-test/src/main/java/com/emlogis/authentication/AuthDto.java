package com.emlogis.authentication;

import java.io.Serializable;

/**
 * Created by user on 02.10.15.
 */
public class AuthDto implements Serializable {

    private String tenant;
    private String login;
    private String password;

    public AuthDto() {
    }

    public AuthDto(String tenant, String login, String password) {
        this.tenant = tenant;
        this.login = login;
        this.password = password;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
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
