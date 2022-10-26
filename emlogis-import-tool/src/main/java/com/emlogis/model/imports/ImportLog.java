package com.emlogis.model.imports;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.context.TenantIdentifierMismatchException;
import org.joda.time.DateTime;

import com.emlogis.common.ImportStatus;
import com.emlogis.common.ImportType;
import com.emlogis.common.UniqueId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Entity()

public class ImportLog {
	
	
	@Id()
    @Column(unique = true, length = 64)
    private String id;
	
	private DateTime importStartDateTime;
	private DateTime importEndDateTime;
	private ImportType importType;
	private float progress;
	private ImportStatus status;
	private String info;
	private String report;
	private long processedRecords;
	private long updatedRecords;
	private byte[] importFile;
	
	@OneToOne
	@JoinColumns({
        @JoinColumn(name="orgConfigId", referencedColumnName="id"),
        @JoinColumn(name="tenantId", referencedColumnName="tenantId")
	})
	private ImportOrganizationConfig importOrgConfig;
	
	
	public ImportLog() {

	}
	
	public ImportLog(DateTime importStartDateTime, DateTime importEndDateTime,
			ImportType importType, float progress, ImportStatus status, String info, String report,
			long processedRecords, long updatedRecords, byte[] importFile,
			ImportOrganizationConfig importOrgConfig) {
		
		this.id = UniqueId.getId();
		this.importStartDateTime = importStartDateTime;
		this.importEndDateTime = importEndDateTime;
		this.importType = importType;
		this.progress = progress;
		this.status = status;
		this.info = info;
		this.report = report;
		this.processedRecords = processedRecords;
		this.updatedRecords = updatedRecords;
		this.importFile = importFile;
		this.importOrgConfig = importOrgConfig;
	}

	public DateTime getImportStartDateTime() {
		return importStartDateTime;
	}
	public void setImportStartDateTime(DateTime importStartDateTime) {
		this.importStartDateTime = importStartDateTime;
	}
	public DateTime getImportEndDateTime() {
		return importEndDateTime;
	}
	public void setImportEndDateTime(DateTime importEndDateTime) {
		this.importEndDateTime = importEndDateTime;
	}
	
	public ImportType getImportType() {
		return importType;
	}

	public void setImportType(ImportType importType) {
		this.importType = importType;
	}

	public float getProgress() {
		return progress;
	}
	public void setProgress(float progress) {
		this.progress = progress;
	}
	public ImportStatus getStatus() {
		return status;
	}
	public void setStatus(ImportStatus status) {
		this.status = status;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getReport() {
		return report;
	}
	public void setReport(String report) {
		this.report = report;
	}
	public byte[] getImportFile() {
		return importFile;
	}
	public void setImportFile(byte[] importFile) {
		this.importFile = importFile;
	}

	public long getProcessedRecords() {
		return processedRecords;
	}

	public void setProcessedRecords(long processedRecords) {
		this.processedRecords = processedRecords;
	}

	public long getUpdatedRecords() {
		return updatedRecords;
	}

	public void setUpdatedRecords(long updatedRecords) {
		this.updatedRecords = updatedRecords;
	}

	public ImportOrganizationConfig getImportOrgConfig() {
		return importOrgConfig;
	}
	public void setImportOrgConfig(ImportOrganizationConfig importOrgConfig) {
		this.importOrgConfig = importOrgConfig;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}	
}
