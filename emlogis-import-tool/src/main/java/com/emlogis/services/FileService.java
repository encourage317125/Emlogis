package com.emlogis.services;

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.Singleton;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.log4j.Logger;

import com.emlogis.common.encrypt.Crypto;
import com.emlogis.model.imports.ImportConfiguration;
import com.emlogis.model.imports.ImportOrganizationConfig;

@Singleton
public class FileService {
	
	private final static Logger logger = Logger.getLogger(FileService.class);
	
	private final static String FILESEP = "/";
	
	private ImportConfiguration importConfiguration;
		
	private String ftpLocation;
	private String ftpLogin;
	private String ftpPassword;
	private String ftpUrl = "";
	
	private String s3Location;
	private String s3AccessId;
	private String s3Secret;
	
	public void startFileService(ImportConfiguration importConfiguration) {
		this.importConfiguration = importConfiguration;
		
		ftpLocation = importConfiguration.getFtpLocation();
		ftpLogin = importConfiguration.getFtpLogin();
		ftpPassword = importConfiguration.getFtpPassword();
		
		ftpUrl = "sftp://" + importConfiguration.getFtpLogin() + 
				":{" + importConfiguration.getFtpPassword() + "}@" +
				importConfiguration.getFtpLocation();
		
		s3Location = importConfiguration.getS3Location();
		try {
			s3AccessId = Crypto.decrypt( importConfiguration.getS3AccessKey() );
			s3Secret = Crypto.decrypt( importConfiguration.getS3SecretKey() );
		} catch (Exception e) {
			logger.error("Error accessing global import configuration S3 information", e);
		}
	}
	
	public byte[] getImportFile(ImportOrganizationConfig orgConfig) {
		
		 String tenant = orgConfig.getTenantId();
		
		logger.debug("Check import files for tenant: " + tenant);
		
		String tenantFolder = orgConfig.getImportFolderURL();
		String tenantFolderURL =  ftpUrl + FILESEP + tenantFolder;
		
		FileSystemManager fsManager = null;
		FileObject tenantFileFolder = null;
		
		FileObject[] tenantFiles = null;
		
		FileObject tenantFile = null;
		
		byte[] importFile = null;
		
		try {
			fsManager = VFS.getManager();
			tenantFileFolder = fsManager.resolveFile( tenantFolderURL );
			tenantFiles = tenantFileFolder.getChildren();

			if (tenantFiles != null && tenantFiles.length > 0) {
				tenantFile = tenantFiles[0];

				importFile = getNextClientFile(tenantFile);
				
				tenantFile.delete();
			}
						
			for(FileObject clientFile: tenantFiles) {
				logger.debug("The next tenant file is: " + clientFile.getName().getBaseName());
			}
			
		} catch (FileSystemException e) {
			logger.error("Error retreiving client file to tenant: " + tenant, e);
		} finally {
			if(tenantFileFolder != null) {
				try {
					tenantFileFolder.close();
				} catch (FileSystemException e) {
					
					logger.warn("Error closing tenant file folder");
				}
			}
			
			if(tenantFile != null) {
				try {
					tenantFile.close();
				} catch (FileSystemException e) {
					
					logger.warn("Error closing tenant file folder");
				}
			}
			
			if(fsManager != null) {
				fsManager.getFilesCache().close();
			}
		}
		
		return importFile;
	}

	private byte[] getNextClientFile(FileObject tenantFile) {
		byte[] importFile = null;
		InputStream inputStream = null;

		try {
			FileContent fileContent = tenantFile.getContent();

			importFile = new byte[(int) fileContent.getSize()];

			inputStream = fileContent.getInputStream();

			while (inputStream.available() > 0)
				inputStream.read(importFile);
		} catch (IOException e) {
			logger.error("Error reading file : "
					+ tenantFile.getName().getBaseName(), e);
			importFile = null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
		
		return importFile;
	}

}
