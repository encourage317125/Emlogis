package com.emlogis.shared.services.eventservice;

import java.io.Serializable;

public class KeyedEvent implements Serializable {
	
	private byte[] 	key;
    private byte[] 	event;

	public KeyedEvent() {
		super();
		// TODO Auto-generated constructor stub
	}
	
    public KeyedEvent(byte[] key, byte[] event) {
		super();
		this.key = key;
		this.event = event;
	}

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}
	
	public byte[] getEvent() {
		return event;
	}

	public void setEvent(byte[] event) {
		this.event = event;
	}

	public String toString() {
		return "key size: " + key.length + " event size: " + event.length;
	}

}

