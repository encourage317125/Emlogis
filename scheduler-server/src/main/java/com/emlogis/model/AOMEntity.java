package com.emlogis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "AOMEntity")
public abstract class AOMEntity extends ACEProtectedEntity {
	
    @Column(nullable = false)
	private	String aomEntityType;		// kind of equiv to className
    	
	// attribute for storing the most trivial implementation of AOM properties
	@Column(nullable = false, length = 1024)
	private String jsonProperties = new String("{}");
	
	@JsonIgnore
	private	transient Map<String,Object> properties;	// in memory properties - must not be stored


	protected AOMEntity() {
		super();
		aomEntityType = this.getClass().getSimpleName();
	}

	public AOMEntity(PrimaryKey primaryKey) {
		super(primaryKey);
		aomEntityType = this.getClass().getSimpleName();
	}

	public String getAomEntityType() {
		return aomEntityType;
	}

	public void setAomEntityType(String aomEntityType) {
		this.aomEntityType = aomEntityType;
	}

	
	public String getJsonProperties() {
		return jsonProperties;
	}

	public void setJsonProperties(String jsonProperties) {
		this.jsonProperties = jsonProperties;
		properties = null;			// reload properties to 'validate' json
		loadProperties();
	}

	public Map<String, Object> getProperties() {
		loadProperties();
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
		saveProperties();
	}

	public Object getProperty(String propName){
		loadProperties();
		return properties.get(propName);
	}

	public void setProperty(String propName, Object value){
		loadProperties();
		properties.put(propName, value);
		saveProperties();
	}
	
	private Map<String,Object> loadProperties(){
		if (properties != null) { return properties; }
		ObjectMapper objMapper = new ObjectMapper();
		try {
			properties = objMapper.readValue(jsonProperties, Map.class);
	        return properties;
	    } catch (IOException e) {
	    	// we are in big trouble ; Unable to load  properties.... let's ignore this pb for now.
	        e.printStackTrace();
	        return new HashMap<String,Object>();
	    } 
	}

	private void saveProperties() {
		ObjectMapper objMapper = new ObjectMapper();
		try {
			String s = objMapper.writeValueAsString(properties);
			jsonProperties = s;
	    } catch (IOException e) {
	    	// we are in  trouble; Unable to save properties .... let's ignore this pb for now.
	        e.printStackTrace();
	    } 
	}


}
