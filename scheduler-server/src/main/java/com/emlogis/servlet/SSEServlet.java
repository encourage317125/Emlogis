package com.emlogis.servlet;


import com.emlogis.common.security.Security;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.eventservice.ASEventService;
import com.emlogis.server.services.eventservice.SSEClient;
import com.emlogis.server.services.eventservice.SSEConsumer;
import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;
import reactor.event.Event;
import reactor.event.selector.Selector;
import reactor.event.selector.Selectors;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ServerSent Event management Servlet. use this url to register the client for receiving events via HTML5 SSE
 * See also SSEHeartbitServlet to keep the connection alive
 *
 * usage: url?tokenId=<tokenId>
 * 
 */
@WebServlet(urlPatterns={"/emlogis/sse"}, asyncSupported=true)

public class SSEServlet extends HttpServlet {
		
    @Inject
    private SessionService 	sessionService;

    @Inject
    private ASEventService 	eventService;

   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	   
	   // get EmLogis token and check user is logged, plus get tenant id + user id
	   String tokenId = /*req.getHeader(Security.TOKEN_HEADER_NAME)*/ req.getParameter(Security.TOKEN_HEADER_NAME);
	   tokenId = req.getParameter("tokenId");
	   if (getSessionService().getSessionInfo(tokenId, false) == null) {
		   resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		   return;
	   }
	   String tenantId = getSessionService().getTenantId(tokenId);

	   // OK, user is logged in.    //content type must be set to text/event-stream & encoding must be set to UTF-8
	   resp.setContentType("text/event-stream");   
	   resp.setCharacterEncoding("UTF-8");
	   
	   // start async. ac will be released by EventService or unregistration of SSEClient
       final AsyncContext ac = req.startAsync();
       ac.setTimeout(0);	// set indefinite timeout 
 
	       // register SSE client into Event Service
       SSEClient sseClient = new SSEClient(tenantId, tokenId, resp, ac);
       getEventService().registerSSEClient(sseClient);
	   // from now on, the EventService should be able to send events to SSE client
       
		// need SelectorBuilder (as KeyBuilder)
		Selector selector = Selectors.R("<.*><" + tenantId + ">.*");
		getEventService().on(selector, new SSEConsumer<Object>(sseClient));

	   // send first an 'acknowledge/registration' event
		Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM).setTenantId(tenantId).setEntityClass("SSEClient").setEventType("Notification").setEntityId("SSEServlet").build();
		try {
			eventService.sendEvent(EventScope.Local, key, Event.wrap("SSE client# " + tokenId + " registered"), "SSEServlet");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return;	   
   }
   
   private SessionService getSessionService() {
       return sessionService;
   }

   private ASEventService getEventService() {
       return eventService;
   }

}
