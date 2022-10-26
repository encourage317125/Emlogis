package com.emlogis.rest.resources.notification;

import com.emlogis.common.facade.notification.NotificationFacade;
import com.emlogis.model.notification.dto.NotificationDTO;
import com.emlogis.model.notification.dto.NotificationMessageDTO;
import com.emlogis.rest.resources.BaseResource;
import com.emlogis.rest.security.Authenticated;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.lang.reflect.InvocationTargetException;

/** Resource for Contracts
 * 
 * @author rjackson
 *
 */

@Path("/notifications")
@Authenticated

public class NotificationResource extends BaseResource {
	@EJB
	NotificationFacade notificationFacade;
	
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

	public NotificationDTO createObject(
			NotificationDTO notificationDTO) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        
        return notificationFacade.createObject(notificationDTO);
	}

}
