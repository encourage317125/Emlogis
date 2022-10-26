package com.emlogis.servlet;


import com.emlogis.common.security.Security;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.eventservice.ASEventService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ServerSent Event management Servlet
 *
 * usage: url?tokenId=<tokenId>
 *
 * This servlet must be invoked periodically to keep the SSE subscription alive for 5mins. 
 * (it is recomended to send a request every 1min)
 *
 */
@WebServlet("/emlogis/sseheartbit")

public class SSEHeartbitServlet extends HttpServlet {
		
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
	   }
	   else {
		   // OK, user is logged in.
		   getEventService().touchSSEClient(tokenId);
		   resp.setStatus(HttpServletResponse.SC_OK);
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
