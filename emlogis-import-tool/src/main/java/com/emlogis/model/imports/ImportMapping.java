package com.emlogis.model.imports;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.ImportType;
import com.emlogis.common.UniqueId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

@Entity()

public class ImportMapping {
	
	public static final String DAY_OF_WEEK 		= "dayOfWeek";
	public static final String HOUR_OF_DAY 		= "hourOfDay";
	public static final String MINUTE 		= "minute";
	
	@Id()
    @Column(unique = true, length = 64)
    private String id;
	
	private ImportType importType;
	private String mappedFields;
	private String schedule;
	
	
	@Lob
    private byte[] mappingRules;
	
	@OneToOne
	@JoinColumns({
        @JoinColumn(name="orgConfigId", referencedColumnName="id"),
        @JoinColumn(name="tenantId", referencedColumnName="tenantId")
	})
	
	private ImportOrganizationConfig importOrgConfig;
	
	public ImportMapping() {
		
	}
	
	public ImportMapping(ImportType importType, String mappedFields, String schedule,
			byte[] mappingRules, ImportOrganizationConfig importOrgConfig) {
		this.id = UniqueId.getId();
		this.importType = importType;
		setSchedule(schedule);
		setMappedFields(mappedFields);
		this.mappingRules = mappingRules;
		this.importOrgConfig = importOrgConfig;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ImportType getImportType() {
		return importType;
	}
	public void setImportType(ImportType importType) {
		this.importType = importType;
	}
	
	public Map<String,String> getMappedFields() {
		
		if( StringUtils.isBlank(mappedFields) ){
			return(getDefaultMappedFields());
		}else {
			return  (Map<String,String>)EmlogisUtils.fromJsonString(mappedFields);
		}	
	}
	
	public void setMappedFields(String mappedFields) {
		if(StringUtils.isBlank(mappedFields)) {
			setMappedFields(getDefaultMappedFields());
		} else {
			// deserialize json to make sure it looks valid
			Map<String,String> settings = (Map<String,String>)EmlogisUtils.fromJsonString(mappedFields);
			this.mappedFields = mappedFields;
		}
	}
	
	public void setMappedFields(Map<String,String> settings) {
		this.mappedFields = EmlogisUtils.toJsonString(settings);
	}
	
	private Map<String,String> getDefaultMappedFields() {
		Map<String,String>  defaultMappedFields =  new HashMap<String, String>();		
		return defaultMappedFields;
	}

	public byte[] getMappingRules() {
		return mappingRules;
	}

	public void setMappingRules(byte[] mappingRules) {
		this.mappingRules = mappingRules;
	}

	public ImportOrganizationConfig getImportOrgConfig() {
		return importOrgConfig;
	}

	public void setImportOrgConfig(ImportOrganizationConfig importOrgConfig) {
		this.importOrgConfig = importOrgConfig;
	}
	
	public Map<String,String> getSchedule() {
		
		if( StringUtils.isBlank(schedule) ){
			return(getDefaultSchedule());
		}else {
			return  (Map<String,String>)EmlogisUtils.fromJsonString(schedule);
		}	
	}

	public void setSchedule(String schedule) {
		if(StringUtils.isBlank(schedule)) {
			setSchedule(getDefaultSchedule());
		} else {
			// deserialize json to make sure it looks valid
			Map<String,String> settings = (Map<String,String>)EmlogisUtils.fromJsonString(schedule);
			this.schedule = schedule;
		}
	}
	
	public void setSchedule(Map<String,String> settings) {
		this.schedule = EmlogisUtils.toJsonString(settings);
	}
	
	private Map<String,String> getDefaultSchedule() {
		Map<String,String>  defaultSchedule =  new HashMap<String, String>();
		defaultSchedule.put(DAY_OF_WEEK, "*");
		defaultSchedule.put(HOUR_OF_DAY, "0-23");
		defaultSchedule.put(MINUTE, "2/2");
		
		return defaultSchedule;
	}
	
	private void setupDefaultSchedule() {
		this.setSchedule(getDefaultSchedule());
	}

	

}
