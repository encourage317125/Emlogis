package com.emlogis.common.services;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.rest.resources.util.DtoMapper;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.server.services.eventservice.ASEventService;

import javax.inject.Inject;

public class BaseService {
	
    @Inject
    private ASEventService eventService;

	public ASEventService getEventService() {
		return eventService;
	}

	public void setEventService(ASEventService eventService) {
		this.eventService = eventService;
	}
	
	// TODO Refactor the Dto convenience methods that are dupliacted in BaseFacade and BaseService
	// so that they are defined only once
	
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

}


