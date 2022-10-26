package com.emlogis.model.notification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

import com.emlogis.common.exceptions.notification.NotificationServiceException;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.notifications.NotificationRole;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class NotificationMessage {
	
	public enum MessagePart {
		Body("body"),
		Subject("subject");
		
		private String value;

		MessagePart(String value) {
			this.value = value;
	    }

	    public String getValue() {
	        return value;
	    }
	}
	
	private final static Logger logger = Logger.getLogger(NotificationMessage.class);

	private NotificationOperation notificationOperation;
	private NotificationCategory notificationCategory;
	private NotificationRole notificationRole;
	
	private String language;
	
	private DateTimeZone timeZone;
	
	private Map<String, String> attributeMap = new HashMap<String, String>();
	
	private Configuration configuration;


	public NotificationMessage(NotificationOperation notificationOperation, NotificationCategory notificationCategory, NotificationRole notificationRole, 
			String language, DateTimeZone timeZone, Map<String, String> attributeMap, Configuration configuration) {
		super();
		this.notificationOperation = notificationOperation;
		this.notificationCategory = notificationCategory;
		this.notificationRole = notificationRole;
		this.language = language;
		this.timeZone = timeZone;
		setAttributeMap(attributeMap);
		this.configuration = configuration;
	}

	public String formatSubject(NotificationDeliveryFormat format) throws NotificationServiceException {
		String retVal = "";
		
		retVal = formatMessage(format, MessagePart.Subject);

		
		return retVal;
	}
	
	public String formatBody(NotificationDeliveryFormat format) throws NotificationServiceException {
		String retVal = "";
		
		
		retVal = formatMessage(format, MessagePart.Body);
		
		return retVal;
	}
	
	private String formatMessage(NotificationDeliveryFormat format, MessagePart messagePart) throws NotificationServiceException {
        String retVal = "";
		
		String templateName = "";
		
		String formatString = "";
		
		formatString = getFomatString(format);

		templateName = notificationCategory.getFilePart() + "_" + notificationRole.getFilePart() + "_" + notificationOperation.getFilePart() +
				"_notification_" + messagePart.getValue() + "_" + formatString;
				
		retVal = getContentFromTemplate(templateName, language);
		
		return retVal;
	}
	
	

	private String getFomatString(NotificationDeliveryFormat format) {
		
		String formatString = "";
		
		switch(format) {
		
		
		case HTML:
			formatString = "html";
			break;
			
		case PLAIN_TEXT:
			formatString = "text";
			break;
			
		case  SMS_TEXT:
			formatString = "sms";
			break;

		default:
			logger.error("Notification has invalid format:" + format.toString());
				
		}
		return formatString;
	}
	
	private String getContentFromTemplate(String templateName, String language) throws NotificationServiceException {
		String templateContent = "";
		
		ByteArrayOutputStream baos = null;
		
		try {
			Template template = configuration.getTemplate(templateName, new Locale(language));
			baos = new ByteArrayOutputStream();
			Writer out = new OutputStreamWriter(baos);
			template.process(attributeMap, out);
			out.flush();
		} catch (IOException | TemplateException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Notification Message template error:", e);
			throw new NotificationServiceException("Notification Message template error", e);
		} 
        
        templateContent =  new String(baos.toByteArray());
		
		return templateContent;
		
	}

	public NotificationOperation getNotificationOperation() {
		return notificationOperation;
	}

	public void setNotificationOperation(NotificationOperation notificationOperation) {
		this.notificationOperation = notificationOperation;
	}

	public NotificationCategory getNotificationCategory() {
		return notificationCategory;
	}

	public void setNotificationCategory(NotificationCategory notificationCategory) {
		this.notificationCategory = notificationCategory;
	}

	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map<String, String> attributeMap) {
		this.attributeMap.clear();
		this.attributeMap.putAll(attributeMap);
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public DateTimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(DateTimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public NotificationRole getNotificationRole() {
		return notificationRole;
	}

	public void setNotificationRole(NotificationRole notificationRole) {
		this.notificationRole = notificationRole;
	}

}
