package com.emlogis.common.services.notification;

public class NotificationConfigInfo {
	
    private	boolean enabled;
    private	String  info;
    private String  deliveryAddress;
    
    // we could have here more data, like a summary of the actual configuration parameters


    public NotificationConfigInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	// cosntructor to be used in case of success
	public NotificationConfigInfo(String info, String  deliveryAddress) {
		super();
		this.enabled = true;
		this.info = info;
		this.deliveryAddress = deliveryAddress;
	}

	// cosntructor to be used in case of failure
	public NotificationConfigInfo(String info) {
		super();
		this.enabled = false;
		this.info = info;
		this.deliveryAddress = deliveryAddress;
	}
    
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
   

}


