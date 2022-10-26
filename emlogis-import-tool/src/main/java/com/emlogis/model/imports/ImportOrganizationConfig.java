package com.emlogis.model.imports;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.inject.internal.UniqueAnnotations;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.UniqueId;
import com.emlogis.services.LocationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity()

public class ImportOrganizationConfig {
	
	public static final String CHARACTER_ESCAPE 		= "characterEscape";
	public static final String CHARACTER_IMPORT 		= "characterImport";
	public static final String CHARACTER_SEPARATOR 		= "characterSeparator";
	public static final String CHARACTER_QUOTE 		= "characterQuote";
	public static final String FILE_NAME		= "fileName";
	
	@Id()
    @Column(unique = true, length = 64)
    private String id;
	
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	DateTime nextImportDateTime;
	String lastImportId;
	LocationType locationType;
	String importFolderURL;
	String tenantId;
	 
	private String properties;
		
	public ImportOrganizationConfig() {

	}

	public ImportOrganizationConfig(String tenantId,
			DateTime nextImportDateTime, String lastImportId,
			LocationType locationType, String importFolderURL, String properties) {
		
		this.id = UniqueId.getId();
		this.tenantId = tenantId;
		this.nextImportDateTime = nextImportDateTime;
		this.lastImportId = lastImportId;
		this.locationType = locationType;
		this.importFolderURL = importFolderURL;

		setProperties(properties);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DateTime getNextImportDateTime() {
		return nextImportDateTime;
	}

	public void setNextImportDateTime(DateTime nextImportDateTime) {
		this.nextImportDateTime = nextImportDateTime;
	}

	public String getImportFolderURL() {
		return importFolderURL;
	}

	public void setImportFolderURL(String importFolderURL) {
		this.importFolderURL = importFolderURL;
	}

	public Map<String,String> getProperties() {
		if( StringUtils.isBlank(properties) ) {
			return getDefaultProperties();
		} else {
			return (Map<String,String>)EmlogisUtils.fromJsonString(properties);
		}
	}

	public void setProperties(String properties) {
		if(StringUtils.isBlank(properties)) {
			setProperties(getDefaultProperties());
		} else {
			// deserialize json to make sure it looks valid
			Map<String,String> settings = (Map<String,String>)EmlogisUtils.fromJsonString(properties);
			this.properties = properties;
		}
	}
	
	public void setProperties(Map<String,String> settings) {
		this.properties = EmlogisUtils.toJsonString(settings);
	}
	
	private Map<String,String> getDefaultProperties() {
		Map<String,String>  defaultproperties =  new HashMap<String, String>();
		defaultproperties.put(CHARACTER_ESCAPE, "\\");
		defaultproperties.put(CHARACTER_IMPORT, ",");
		defaultproperties.put(CHARACTER_SEPARATOR, ",");
		defaultproperties.put(CHARACTER_QUOTE, "\"");
		
		return defaultproperties;
	}

	public String getLastImportId() {
		return lastImportId;
	}

	public void setLastImportId(String lastImportId) {
		this.lastImportId = lastImportId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	
}
