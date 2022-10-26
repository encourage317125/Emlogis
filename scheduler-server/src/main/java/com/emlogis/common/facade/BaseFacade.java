package com.emlogis.common.facade;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.eventservice.ASEventService;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public abstract class BaseFacade {

    private final static org.apache.log4j.Logger logger = Logger.getLogger(BaseFacade.class);

    @Inject
    private SessionService sessionService;

    @Inject
    private ASEventService eventService;

    public SessionService getSessionService() {
		return sessionService;
	}

	public ASEventService getEventService() {
		return eventService;
	}

	protected String getActualUserId() {
        return sessionService.getActualUserId();
    }

    protected String getActualUserName() {
        return sessionService.getActualUserName();
    }

    // methods setting createdBy / updatedBy / ownedBy attributes
    // TODO for the sake of debugging we store name vs Id. to be replaced by Id in future
    protected void setCreatedBy(BaseEntity entity) {
		entity.setCreatedBy(getActualUserName());
		setUpdatedBy(entity);
	}

    protected void setUpdatedBy(BaseEntity entity) {
		entity.setUpdatedBy(getActualUserName());
	}

    protected void setOwnedBy(BaseEntity entity, String ownerId) {
		entity.setOwnedBy(ownerId != null ? ownerId : getActualUserName());
	}

    protected void setOwnedBy(BaseEntity entity) {
		setOwnedBy(entity, null);
	}
    
  

    /**
     * map() maps a source pojo into a target pojo, by copying fields defined into target DataTransferObjects, from source pojo
     * @param src
     * @param tgtClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected <E, T> T toDto(E src, Class<T> tgtClass) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        return new DtoMapper<E, T>().map(src, tgtClass);
    }

	protected <E, T> T toDto(E src, Map<Class<? extends E>, Class<? extends T>> classMap)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		return new DtoMapper<E, T>().map(src, classMap);
	}

    /**
     * map() maps a source ResultSet into a target ResultSetDto pojo,
     * @param src
     * @param tgtClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    protected <E, T> ResultSetDto<T> toResultSetDto(ResultSet<E> src, Class<T> tgtClass) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return toResultSetDto(src, tgtClass, new DtoMapper<E, T>());
    }

    protected <E, T> ResultSetDto<T> toResultSetDto(ResultSet<E> src, Class<T> tgtClass, DtoMapper<E, T> dtoMapper)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return dtoMapper.mapResultSet(src, tgtClass);
    }

    /**
     * map() maps a collection of heterogeneous source ResultSet into a target ResultSetDto pojo,
     * @param src
     * @param classMap
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    protected <E, T> ResultSetDto<T> toResultSetDto(ResultSet<E> src,
                                                    Map<Class<? extends E>, Class<? extends T>> classMap)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return new DtoMapper<E, T>().mapResultSet(src, classMap);
    }

    /**
     * map() maps a collection of homogeneous source pojos (pojo of same class) into a collection of target TransferDataObjects
     * @param srcCol
     * @param tgtClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected <E, T> Collection<T> toCollectionDto(Collection<E> srcCol, Class<T> tgtClass)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return new DtoMapper<E, T>().map(srcCol, tgtClass);
    }

    protected String getMessage(String code, Object... params) {
        return sessionService.getMessage(code, params);
    }

}
