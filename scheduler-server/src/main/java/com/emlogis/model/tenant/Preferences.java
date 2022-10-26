package com.emlogis.model.tenant;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Preferences {
	
	private String lang = "en";

	public Preferences() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}

