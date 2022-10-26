package com.emlogis.model.notification;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()

public class SendNotification extends Notification implements Serializable {
	
	@Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime tobeDeliveredOn = new DateTime();
	
	@Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deliveredOn;
	
	@Column()
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastRetryDateTime = new DateTime();
	
	private int retryCount = 0;

	public DateTime getTobeDeliveredOn() {
		return tobeDeliveredOn;
	}

	public void setTobeDeliveredOn(DateTime tobeDeliveredOn) {
		this.tobeDeliveredOn = tobeDeliveredOn;
	}

	public DateTime getDeliveredOn() {
		return deliveredOn;
	}

	public void setDeliveredOn(DateTime deliveredOn) {
		this.deliveredOn = deliveredOn;
	}

	public DateTime getLastRetryDateTime() {
		return lastRetryDateTime;
	}

	public void setLastRetryDateTime(DateTime lastRetryDateTime) {
		this.lastRetryDateTime = lastRetryDateTime;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
}
