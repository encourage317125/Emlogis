package com.emlogis.model.notification.template;

import com.emlogis.common.notifications.NotificationDeliveryFormat;
import com.emlogis.common.notifications.NotificationCategory;
import com.emlogis.common.notifications.NotificationOperation;
import com.emlogis.common.notifications.NotificationRole;
import com.emlogis.model.common.BaseEntityBean;
import com.emlogis.model.notification.NotificationMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Locale;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class NotificationMessageTemplate  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "templateName", nullable = false)
    private String templateName;

    @Lob
    @Column(name = "template", nullable = false)
    private byte[] template;

    private Locale locale;

    @Enumerated(EnumType.STRING)
    private NotificationOperation notificationOperation;
    
    @Enumerated(EnumType.STRING)
    private NotificationCategory notificationCategory;

    @Enumerated(EnumType.STRING)
    private NotificationRole notificationRole;

    @Enumerated(EnumType.STRING)
    private NotificationDeliveryFormat deliveryFormat;

    @Enumerated(EnumType.STRING)
    private NotificationMessage.MessagePart messagePart;

    public NotificationMessageTemplate() {
        super();
    }

    public NotificationMessageTemplate(NotificationOperation operation, NotificationCategory category, NotificationRole notificationRole, String templateName, byte[] template, Locale locale,
                                       NotificationDeliveryFormat deliveryFormat,
                                       NotificationMessage.MessagePart messagePart) {
        this();
        this.notificationOperation = operation;
        this.notificationCategory = category;
        this.notificationRole = notificationRole;
        this.templateName = templateName;
        this.template = template;
        this.locale = locale;
        this.deliveryFormat = deliveryFormat;
        this.messagePart = messagePart;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public byte[] getTemplate() {
        return template;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public NotificationDeliveryFormat getDeliveryFormat() {
        return deliveryFormat;
    }

    public void setDeliveryFormat(NotificationDeliveryFormat deliveryFormat) {
        this.deliveryFormat = deliveryFormat;
    }

    public NotificationMessage.MessagePart getMessagePart() {
        return messagePart;
    }

    public void setMessagePart(NotificationMessage.MessagePart messagePart) {
        this.messagePart = messagePart;
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
	
    public NotificationRole getNotificationRole() {
        return notificationRole;
    }

    public void setNotificationRole(NotificationRole notificationRole) {
        this.notificationRole = notificationRole;
    }
	

	@Override
    public boolean equals(Object obj) {
        if (obj instanceof NotificationMessageTemplate) {
            NotificationMessageTemplate other = (NotificationMessageTemplate) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getLocale(), other.getLocale());
            builder.append(getDeliveryFormat(), other.getDeliveryFormat());
            builder.append(getMessagePart(), other.getMessagePart());
            builder.append(getTemplateName(), other.getTemplateName());
            builder.append(getTemplate(), other.getTemplate());
            builder.append(getNotificationOperation(), other.getNotificationOperation());
            builder.append(getNotificationCategory(), other.getNotificationCategory());
            builder.append(getNotificationOperation(), other.getNotificationOperation());
            builder.append(getNotificationRole(), other.getNotificationRole());

            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getLocale());
        builder.append(getMessagePart());
        builder.append(getDeliveryFormat());
        builder.append(getTemplateName());
        builder.append(getTemplate());
        builder.append(getNotificationOperation());
        builder.append(getNotificationCategory());
        builder.append(getNotificationOperation());
        builder.append(getNotificationRole());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("type", getNotificationOperation()).
                append("subject", getNotificationCategory()).
                append("locale", getLocale()).
                append("deliveryFormat", getMessagePart()).
                append("messagePart", getDeliveryFormat()).
                append("template name ", getTemplateName()).
                append("template value", getTemplate()).
                toString();
    }
}
