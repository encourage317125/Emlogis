package com.emlogis.model.tenant.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class ChangePasswordDto implements Serializable {

    private	String tenantId;
    private	String login;
    private	String oldPassword;
    private	String newPassword;
    private	boolean isNewPasswordHashed = false; //  for now, lets' consider the new password is in plain text

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

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

	public boolean isNewPasswordHashed() {
		return isNewPasswordHashed;
	}

	public void setNewPasswordHashed(boolean isNewPasswordHashed) {
		this.isNewPasswordHashed = isNewPasswordHashed;
	}

}
