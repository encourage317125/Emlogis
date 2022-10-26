package com.emlogis.common.services.notification.template;

import static com.emlogis.common.Constants.BODY_MATCH;
import static com.emlogis.common.Constants.DELIVERY_FORMAT;
import static com.emlogis.common.Constants.FILE_PATH;
import static com.emlogis.common.Constants.HTML_MATCH;
import static com.emlogis.common.Constants.MESSAGE_PART;
import static com.emlogis.common.Constants.NOTIFICATION_CATEGORY;
import static com.emlogis.common.Constants.NOTIFICATION_OPERATION;
import static com.emlogis.common.Constants.NOTIFICATION_ROLE;
import static com.emlogis.common.Constants.SMS_MATCH;
import static com.emlogis.common.Constants.SUBJECT_MATCH;
import static com.emlogis.common.Constants.TEXT_MATCH;
import static com.emlogis.common.Constants.lANGUAGE;
import static com.emlogis.workflow.WflUtil.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import com.emlogis.common.FileUtil;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.notifications.NotificationRole;
import com.emlogis.model.notification.NotificationMessage;
import com.emlogis.model.notification.template.NotificationMessageTemplate;

@Startup
@Singleton
@DependsOn("WorkflowInitServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
public class NotificationMessageTemplateInitBean {
	
	private final static Logger logger = Logger.getLogger(NotificationMessageTemplateInitBean.class);
	
	Map<String, Map<String, String>> messageTemplateMap = new LinkedHashMap<>();
	
	@Resource
	private EJBContext context;
	
	@EJB
	NotificationMessageTemplateServiceImpl templateService;

	@PostConstruct
    void init() {
		UserTransaction userTransaction = context.getUserTransaction();
		logger.debug("Reading Notification Message Templates");
	
		byte[] templateContent;
		
		NotificationMessageTemplate template;
		
		String templatePath;
		Locale locale;
		NotificationDeliveryFormat deliveryFormat;
		NotificationMessage.MessagePart messagePart;
		NotificationOperation notificationOperation;
		NotificationCategory notificationCategory;
		NotificationRole notificationRole;
		
		try {
			// delete any templates stored in the database
			// start fresh when restarted
			//userTransaction.begin();
			templateService.deleteAll();
			//userTransaction.commit();
			
			// Read file with template mapping
			loadTemplates("/notifications");
			
			Map<String, String> templateFieldsMap = null;

			//userTransaction.begin();
			for (Map.Entry<String, Map<String, String>> templateEntry : messageTemplateMap.entrySet()) {
				try {
                    templateFieldsMap = templateEntry.getValue();

                    templatePath = templateFieldsMap.get(FILE_PATH);

                    locale = new Locale(templateFieldsMap.get(lANGUAGE));

                    deliveryFormat = NotificationDeliveryFormat.valueOf(templateFieldsMap.get(DELIVERY_FORMAT));
                    messagePart = NotificationMessage.MessagePart.valueOf(templateFieldsMap.get(MESSAGE_PART));
                    notificationOperation = NotificationOperation.valueOf(templateFieldsMap.get(NOTIFICATION_OPERATION));
                    notificationCategory = NotificationCategory.valueOf(templateFieldsMap.get(NOTIFICATION_CATEGORY));
                    notificationRole = NotificationRole.valueOf(templateFieldsMap.get(NOTIFICATION_ROLE));

                    // Read each template
                    templateContent = FileUtil.readPhysicialFileContentByName(templatePath).getBytes(UTF_8);

                    template = new NotificationMessageTemplate(notificationOperation, notificationCategory,
                            notificationRole, templateEntry.getKey(), templateContent, locale, deliveryFormat,
                            messagePart);
                    templateService.create(template);
				} catch (URISyntaxException | IOException |  SecurityException | IllegalStateException |
                        IllegalArgumentException e) {
					logger.error("Error reading Notification Message Template");
					logger.error("File Path: " + templateFieldsMap.get(FILE_PATH));
					logger.error("Error while creating Notification Message Templates",e);
				} catch (Exception ex) {
					logger.error("Fatal Error while creating Notification Message Templates", ex);
				}
			}
			//userTransaction.commit();
		//} catch (NotSupportedException | SystemException | SecurityException | IllegalStateException |
        //        RollbackException | HeuristicMixedException | HeuristicRollbackException | IllegalArgumentException e) {
		//	logger.error("Error while reading Notification Message Templates", e);
		} catch (Exception ex) {
			logger.error("Fatal Error while reading Notification Message Templates", ex);
		}
	}
	
	private void loadTemplates(String notificationRescourcePath) {
		Map<String, String> templateFields;
        
        String filePath, templateName, fileName, deliveryFormat, messagePart;
        
        File notificationResourceDirectory = FileUtil.getFileFromVirtualFileSystem(notificationRescourcePath);
        File[] languageDirectories = FileUtil.getDirectories(notificationResourceDirectory);
        File[] notificationOperationDirectories;
        File[] categoryDirectories;
        File[] roleDirectories;
        File[] templateFiles;
        
        for (File languageDirectory : languageDirectories) {
        	categoryDirectories = FileUtil.getDirectories(languageDirectory);
        	for (File categoryDirectory : categoryDirectories){
        		roleDirectories = FileUtil.getDirectories(categoryDirectory);
        		for (File roleDirectory : roleDirectories) {
	        		notificationOperationDirectories = FileUtil.getDirectories(roleDirectory);
		            for (File notificationOperationDirectory : notificationOperationDirectories) {
		                templateFiles = FileUtil.getFiles(notificationOperationDirectory);
		                for (File templateFile : templateFiles) {
		                    templateFields = new LinkedHashMap<>();
		                    filePath = templateFile.getPath();
		                    fileName = templateFile.getName();
		                    templateName = fileName.substring(0, fileName.indexOf('.'));
		
		                    templateFields.put(FILE_PATH, filePath);
		                    templateFields.put(lANGUAGE, languageDirectory.getName());
		                    templateFields.put(NOTIFICATION_OPERATION, notificationOperationDirectory.getName().toUpperCase());
		                    templateFields.put(NOTIFICATION_CATEGORY, categoryDirectory.getName().toUpperCase());
		                    templateFields.put(NOTIFICATION_ROLE, roleDirectory.getName().toUpperCase());
		
		                    if (templateName.contains(HTML_MATCH)) {
		                        deliveryFormat = NotificationDeliveryFormat.HTML.toString();
		                    } else if (templateName.contains(TEXT_MATCH)) {
		                        deliveryFormat = NotificationDeliveryFormat.PLAIN_TEXT.toString();
		                    } else  if (templateName.contains(SMS_MATCH)){
		                        deliveryFormat = NotificationDeliveryFormat.SMS_TEXT.toString();
		                    } else {
		                    	logger.error("Bad notification template name : " + templateName);
		                    	continue;
		                    }
		
		                    if (templateName.contains(BODY_MATCH)) {
		                        messagePart = NotificationMessage.MessagePart.Body.toString();
		                    }  else if (templateName.contains(SUBJECT_MATCH)) {
		                        messagePart = NotificationMessage.MessagePart.Subject.toString();
		                    } else {
		                    	logger.error("Bad notification template name: " + templateName);
		                    	continue;
		                    }
		
		                    templateFields.put(DELIVERY_FORMAT, deliveryFormat);
		                    templateFields.put(MESSAGE_PART, messagePart);
		
		                    messageTemplateMap.put(templateName, templateFields);
		                }
		            }
	            }
        	}
        }
	}

}
