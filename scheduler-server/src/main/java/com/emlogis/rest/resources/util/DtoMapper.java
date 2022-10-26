package com.emlogis.rest.resources.util;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.dto.ResultSetDto;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.*;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DtoMapper<E, T> {

    private final static Logger logger = Logger.getLogger(DtoMapper.class);

    @SuppressWarnings("unchecked")
    private final static Map<Class, Method[]> classMethodCache = BeanUtils.createCache();

    @SuppressWarnings("unchecked")
    private final static Map<Class, Set<String>> classPropertiesCache = BeanUtils.createCache();

    private Map<String, String> fieldsMapper = new HashMap<>();
    private Set<String> exceptFields = new HashSet<>();

    public DtoMapper() {}

    public DtoMapper(String[]... mappingFields) {
        for (String[] fields : mappingFields) {
            registerNestedDtoMapping(fields[0], fields[1]);
        }
    }

    public DtoMapper(Map<String, String> fieldsMapper) {
        this.fieldsMapper = fieldsMapper;
    }

    public void registerNestedDtoMapping(String dtoField, String entityField) {
        fieldsMapper.put(dtoField, entityField);
    }

    public void registerExceptDtoFieldForMapping(String dtoField) {
        exceptFields.add(dtoField);
    }

    /**
     * map() maps a source pojo into a target pojo, by copying fields defined in tgt pojo from source pojo
     * @param src
     * @param tgtClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    public T map(E src, Class<? extends T> tgtClass) {
        if (src == null || tgtClass == null) {
            return null;
        }
        String debugSrcClassName = null, debugFieldName = null;	// variables for facilitating debugging
        try {
            Class srcClass = src.getClass();
            T target = tgtClass.newInstance();

            debugSrcClassName = srcClass.getSimpleName();
//        	System.out.println("DtoMapper-> mapping " + debugSrcClassName + " to " + tgtClass.getSimpleName());
            for (String name : getClassProperties(target)) {
//            	System.out.println("DtoMapper-> processing " + name);
                if (exceptFields.contains(name)) {
                    continue;
                }
            	debugFieldName = name;
                if ("class".equals(name)) {
                    // This is reflecting of method getClass() of class Object. We need to exclude it.
                    continue;
                }
                Method setter = getAccessor(tgtClass, name, true);
                Object value;
                if (fieldsMapper.containsKey(name)) {
                    String entityFieldName = fieldsMapper.get(name);
                    Method getter = getAccessor(srcClass, entityFieldName, false);
                    value = getter.invoke(src);
                    Class setterClass = setter.getParameterTypes()[0];
                    if (value instanceof Collection) {
                        Collection collection = null;
                        if (setterClass == Set.class) {
                            collection = new HashSet<>();
                        } else if (setterClass == Collection.class || setterClass == List.class) {
                            collection = new ArrayList<>();
                        }
                        if (collection != null) {
                            Class collectionElementClass = null;
                            Field field = tgtClass.getDeclaredField(name);
                            if (field != null && field.getGenericType() instanceof ParameterizedTypeImpl) {
                                ParameterizedTypeImpl type = (ParameterizedTypeImpl) field.getGenericType();
                                collectionElementClass = (Class) type.getActualTypeArguments()[0];
                            }
                            for (Object srcElement : (Iterable) value) {
                                if (collectionElementClass == null) {
                                    collectionElementClass = getDtoClass(srcElement);
                                }
                                Object tgtElement = new DtoMapper().map(srcElement, collectionElementClass);
                                collection.add(tgtElement);
                            }
                        }
                        value = collection;
                    } else {
                        value = new DtoMapper().map(value, setter.getParameterTypes()[0]);
                    }
                } else {
                    Method getter = getAccessor(srcClass, name, false);
//                    if (getter == null) {	This doesn't work as getAccessor throw an exception if getter not found
//                    	System.out.println("DtoMapper-> skipping attribute: " + name + " because no getter on source");
//                    	continue;	// silently skip this attribute if there is no getter on source class (should issue a warning though)
//                    }
                    value = getter.invoke(src);
                    if (value instanceof DateTime) {
                        value = ((DateTime) value).getMillis();
                    } else if (value instanceof LocalTime) {
                        value = ((LocalTime) value).getMillisOfDay();
                    } else if (value instanceof LocalDate) {
                        value = ((LocalDate) value).toDate().getTime();
                    } else if (value instanceof DateTimeZone) {
                        value = ((DateTimeZone) value).getID();
                    } else if (value instanceof Minutes) {
                        value = ((Minutes) value).getMinutes();
                    }
                }
//            	System.out.println("DtoMapper-> call setter: " + name);
                Class<?> paramType = setter.getParameterTypes()[0];
                if (!(paramType.isPrimitive() && value == null)) {
                    if (paramType == Long.class && value instanceof Integer) {
                        value = ((Integer) value).longValue();
                    }
                    setter.invoke(target, value);
                }
//            	System.out.println("DtoMapper-> processing: " + name + " done.");

            }
//        	System.out.println("DtoMapper-> returning Dto.");
            return target;
        } catch (Exception e) {
            logger.error("Error mapping DTO object on: " + debugSrcClassName + "." + debugFieldName + " to: "
                    + tgtClass.getSimpleName(), e);
            throw new RuntimeException(e);
        }
    }
    
    public T map(E src, Map<Class<? extends E>, Class<? extends T>> classMap) {
    	if (classMap == null || classMap.isEmpty()) {
    		return null;
    	}
    	
	    Class<? extends T> tgtClass = classMap.get(src.getClass());
	    return map(src, tgtClass);
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
    public ResultSetDto<T> mapResultSet(ResultSet<E> src, Class<T> tgtClass) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	ResultSetDto<T> rsDto = new ResultSetDto<>();
    	Collection<T> col = map(src.getResult(), tgtClass);
    	rsDto.setResult(col);
    	rsDto.setTotal(src.getTotal());
        return rsDto;
    }

    /**
     * map() maps a collection of heterogeneous source ResultSet into a target ResultSetDto pojo,
     * @param src
     * @param classMapping map of pojo class to dto class mapping
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public ResultSetDto<T> mapResultSet(ResultSet<E> src, Map<Class<? extends E>, Class<? extends T>> classMapping)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResultSetDto<T> rsDto = new ResultSetDto<>();
        Collection<T> col = map(src.getResult(), classMapping);
        rsDto.setResult(col);
        rsDto.setTotal(src.getTotal());
        return rsDto;
    }

    /**
     * map() maps a collection of homogeneous source pojos (pojo of same class) into a collection of target pojos
     * @param srcCol
     * @param tgtClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public Collection<T> map(Collection<E> srcCol, Class<? extends T> tgtClass) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Collection<T> tgtCol = new ArrayList<>();
        if (srcCol != null && !srcCol.isEmpty()) {
            for (E src : srcCol) {
                T tgt = map(src, tgtClass);
                tgtCol.add(tgt);
            }
        }
        return tgtCol;
    }

	/**
	 * map() maps a collection of heterogeneous source pojos (pojo of potentially different classes) into a collection of target pojos
	 * @param srcCol
	 * @param classMapping map of pojo class to dto class mapping
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Collection<T> map(Collection<E> srcCol, Map<Class<? extends E>, Class<? extends T>> classMapping) throws
            InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    Collection<T> tgtCol = new ArrayList<>();
	    if (srcCol != null && !srcCol.isEmpty()) {
	        for (E src : srcCol) {
	        	Class<? extends T> tgtClass = classMapping.get(src.getClass());
	            T tgt = map(src, tgtClass);
	            tgtCol.add(tgt);
	        }
	    }
	    return tgtCol;
	}

	private Method getAccessor(Class clazz, String propName, boolean setter) {
		try {
			// build accessor method name
			if (StringUtils.isBlank(propName)) {
				throw new IllegalArgumentException("Can't get Instance property. propertyName is empty or null");
			}
			String mName = (setter ? "set" : "get") + propName.substring(0, 1).toUpperCase() + propName.substring(1);

			Method[] allMethods = getClassMethods(clazz);
		    for (Method method : allMethods) {
		    	if (method.getName().equals(mName)) {
		    		return method;
		    	}
		    }
            if (!setter) {
                mName = "is" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
                try {
                    return clazz.getMethod(mName);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Can't get property: '" + propName +
                            "', attribute or method not found");
                }
            }
            throw new IllegalArgumentException("Can't get property: '" + propName + "', attribute or method not found");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Can't get property: '" + propName + "', IllegalArgumentException", e);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new SecurityException("Can't get property: '" + propName + "', Security Exception", e);
		}
	}

    private Method[] getClassMethods(Class clazz) {
        Method[] result = classMethodCache.get(clazz);
        if (result == null) {
            result = clazz.getMethods();
            classMethodCache.put(clazz, result);
        }
        return result;
    }

    private Set<String> getClassProperties(Object target) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Class clazz = target.getClass();
        Set<String> result = classPropertiesCache.get(clazz);
        if (result == null) {
            @SuppressWarnings("unchecked")
            Map<String, String> propMap = BeanUtils.describe(target);
            result = propMap.keySet();
            classPropertiesCache.put(clazz, result);
        }
        return result;
    }

    private Class getDtoClass(Object entity) {
        Class result = null;
        if (entity instanceof BaseEntity) {
            result = ((BaseEntity) entity).getReadDtoClass();
            if (result == null) {
                String className = entity.getClass().getSimpleName();
                String packageName = entity.getClass().getPackage().getName();
                try {
                    result = Class.forName(packageName + ".dto." + className + "Dto");
                } catch (Exception e) {
                    logger.error("",e);
                }
            }
        }
        return result;
    }
}


