package com.emlogis.common.services.notification.template;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.emlogis.model.notification.template.NotificationMessageTemplate;

import freemarker.cache.TemplateLoader;

@Startup
@Singleton
@DependsOn({"NotificationMessageTemplateInitBean"})
@Local(TemplateLoader.class)
public class NotificationMessageTemplateLoader implements TemplateLoader {

	private final static Logger logger = Logger.getLogger(NotificationMessageTemplateLoader.class);

	@EJB
	private NotificationMessageTemplateServiceImpl templateService;

	@Override
	public Object findTemplateSource(String name) throws IOException {
		logger.info("NotificationMessageTemplateLoader -> findTemplateSource : ");
		return templateService.findTemplateByName(name);
	}

	@Override
	public long getLastModified(Object templateSource) {
		logger.info("NotificationMessageTemplateLoader -> getLastModified : ");
		NotificationMessageTemplate notificationTemplate = (NotificationMessageTemplate) templateSource;
		return new DateTime().getMillis();
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		logger.info("NotificationMessageTemplateLoader -> getReader : ");
		NotificationMessageTemplate notificationTemplate = (NotificationMessageTemplate) templateSource;
		return new StringReader(new String(notificationTemplate.getTemplate()));
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		logger.info("NotificationMessageTemplateLoader -> closeTemplateSource : ");
	}


}
