package com.emlogis.model.tenant.dto;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

@XmlRootElement
public class PasswordResetDto implements Serializable {

    private	String info;
    private String emailAddress;
    
	public PasswordResetDto() {
		super();
	}

    public PasswordResetDto(String info) {
        this.info = info;
    }

    public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
