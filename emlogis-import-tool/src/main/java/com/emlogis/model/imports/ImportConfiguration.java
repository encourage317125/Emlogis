package com.emlogis.model.imports;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;

import com.emlogis.common.UniqueId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity()
public class ImportConfiguration {
	
	@Id()
    @Column(unique = true, length = 64)
    private String id;
	
	private String pollExpressionMinutes = "*/5";
	private String ftpLocation;

	private String ftpLogin;
	private String ftpPassword;
	private String s3Location;
	private String s3AccessKey;
	private String s3SecretKey;
	
	public ImportConfiguration() {

	}

	public ImportConfiguration(String pollExpressionMinutes,
			String ftpLocation, String ftpLogin, String ftpPassword,
			String s3Location, String s3AccessKey, String s3SecretKey) {

		this.id = UniqueId.getId();
		this.pollExpressionMinutes = pollExpressionMinutes;
		this.ftpLocation = ftpLocation;
		this.ftpLogin = ftpLogin;
		this.ftpPassword = ftpPassword;
		this.s3Location = s3Location;
		this.s3AccessKey = s3AccessKey;
		this.s3SecretKey = s3SecretKey;
				
	}
	
	public String getPollExpressionMinutes() {
		return pollExpressionMinutes;
	}
	public void setPollExpressionMinutes(String pollExpressionMinutes) {
		this.pollExpressionMinutes = pollExpressionMinutes;
	}
	public String getFtpLocation() {
		return ftpLocation;
	}
	public void setFtpLocation(String ftpLocation) {
		this.ftpLocation = ftpLocation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
		
	public String getFtpLogin() {
		return ftpLogin;
	}
	public void setFtpLogin(String ftpLogin) {
		this.ftpLogin = ftpLogin;
	}
	public String getFtpPassword() {
		return ftpPassword;
	}
	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	public String getS3Location() {
		return s3Location;
	}

	public void setS3Location(String s3Location) {
		this.s3Location = s3Location;
	}

	public String getS3AccessKey() {
		return s3AccessKey;
	}

	public void setS3AccessKey(String s3AccessKey) {
		this.s3AccessKey = s3AccessKey;
	}

	public String getS3SecretKey() {
		return s3SecretKey;
	}

	public void setS3SecretKey(String s3SecretKey) {
		this.s3SecretKey = s3SecretKey;
	}	
	
}
