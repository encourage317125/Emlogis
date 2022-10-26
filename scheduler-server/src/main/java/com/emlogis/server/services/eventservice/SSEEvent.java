package com.emlogis.server.services.eventservice;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
class SSEEvent {

	private	String 				id;
	private	String 				key;
	private	Map<String,Object> 	headers;
	private Object 				data;
	private	String				thread;
	
	public SSEEvent() {
		super();
		thread = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
	}

	public SSEEvent(String id, String key, Map<String, Object> headers, Object data) {
		this();
		this.id = id;
		this.key = key;
		this.headers = headers;
		this.data = data;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	

}