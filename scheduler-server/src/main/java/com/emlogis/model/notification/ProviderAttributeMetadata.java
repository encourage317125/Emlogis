package com.emlogis.model.notification;

public class ProviderAttributeMetadata {

	private String	name;
	private String	label;
	private String	type;			// type = string | int | boolean | phoneNb
	private String	defaultVal;
	private boolean isRequired = true;

	public ProviderAttributeMetadata() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ProviderAttributeMetadata(String name, String label,
			String type, String defaultVal ) {
		super();
		this.name = name;
		this.label = label;
		this.type = type;
		this.defaultVal = defaultVal;
	}

	public ProviderAttributeMetadata(String name, String label,
			String type, String defaultVal, boolean isRequired ) {
		super();
		this.name = name;
		this.label = label;
		this.type = type;
		this.defaultVal = defaultVal;
		this.isRequired = isRequired;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(String defaultVal) {
		this.defaultVal = defaultVal;
	}

	public boolean isRequired() {
		return isRequired;
	}
	
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}   	
	
}