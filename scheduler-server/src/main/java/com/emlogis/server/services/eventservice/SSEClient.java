package com.emlogis.server.services.eventservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SSEClient {
	
	private final Logger logger = LoggerFactory.getLogger(SSEClient.class);
	
	private	String				tenantId;
	private	String				tokenId;
	private HttpServletResponse resp;
	private	AsyncContext 		ac;
	private long				updated;		// date/time the client has indicated this SSEclient is alive
	
	private List<SSEConsumer<?>> consumers = new ArrayList();
	
	public SSEClient(String tenantId, String tokenId, HttpServletResponse resp, AsyncContext ac) {
		super();
		this.tenantId = tenantId;
		this.tokenId = tokenId;
		this.resp = resp;
		this.ac = ac;
		touch();
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public HttpServletResponse getResp() {
		return resp;
	}

	public void setResp(HttpServletResponse resp) {
		this.resp = resp;
	}

	public AsyncContext getAc() {
		return ac;
	}

	public void setAc(AsyncContext ac) {
		this.ac = ac;
	}
	
	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}

	public void touch() {
		updated = System.currentTimeMillis();
	}

	public List<SSEConsumer<?>> getConsumers() {
		return consumers;
	}

	public void addConsumer(SSEConsumer<?> consumer) {
		consumers.add(consumer);
	}
	
	public void removeConsumer(SSEConsumer<?> consumer) {
		consumers.remove(consumer);
	}
	
    public void sendSSEEvent(String event) throws IOException {
    	
		String msg = "data:" + event + "\n\n";
		logger.debug( "Sending: " + msg);
	
		PrintWriter writer = resp.getWriter();
		// each message that we send must ends with \n\n.
		writer.write(msg);
		writer.flush();
		resp.flushBuffer();
    }
    
}

