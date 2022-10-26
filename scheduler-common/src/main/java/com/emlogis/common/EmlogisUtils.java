package com.emlogis.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.*;

public class EmlogisUtils {

    public static Object getPathFieldValue(String name, Object instance) throws IllegalAccessException {
        String[] pathFieldNames = name.split("[.]");
        Object currentValue = instance;
        for (String pathFieldName : pathFieldNames) {
            if (currentValue == null) {
                break;
            }
            Class clazz = currentValue.getClass();
            List<Field> fields = EmlogisUtils.getAllFields(clazz);
            currentValue = getFieldValue(fields, pathFieldName, currentValue);
        }
        return currentValue;
    }

    public static Object getFieldValue(List<Field> fields, String name, Object instance) throws IllegalAccessException {
        Field field = EmlogisUtils.findFieldByName(fields, name);
        if (field != null) {
            field.setAccessible(true);
            return field.get(instance);
        } else {
            return null;
        }
    }

    public static List<Field> getAllFields(Class clazz) {
        List<Field> result = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        result.addAll(Arrays.asList(fields));

        Class superclass = clazz.getSuperclass();
        if (!superclass.equals(Object.class)) {
            List<Field> superFields = getAllFields(superclass);
            result.addAll(superFields);
        }

        return result;
    }

    public static Field searchAnnotatedField(Class clazz, Class<? extends Annotation> annotationClass) {
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation declaredAnnotation : annotations) {
                if (declaredAnnotation.annotationType() == annotationClass) {
                    return field;
                }
            }
        }

        return null;
    }

    public static Field findFieldByName(List<Field> fields, String name) {
        for (Field field : fields) {
            if (StringUtils.equals(field.getName(), name)) {
                return field;
            }
        }
        return null;
    }

    public static Field findFieldByName(Class clazz, String name) {
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            if (StringUtils.equals(field.getName(), name)) {
                return field;
            }
        }
        return null;
    }

    public static String toJsonString(Object object)  {
    	if (object == null) return null;
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JodaModule());
        try {
            return objMapper.writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Object fromJsonString(String json) {
    	return fromJsonString(json, Object.class);
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JodaModule());
        try {
            return objMapper.readValue(json, clazz);
        } catch (IOException e) {
            // we are in trouble ; Unable to load  object
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJsonString(String json, TypeReference typeReference) {
        ObjectMapper objMapper = new ObjectMapper();
        objMapper.registerModule(new JodaModule());
        try {
            return objMapper.readValue(json, typeReference);
        } catch (IOException e) {
            // we are in trouble ; Unable to load  object
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static byte[] serializeObject(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public static Object deserializeObject(byte[] serObject) {
        if (serObject == null) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(serObject);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public static int findInArray(Object[] array, Object key) {
        for (int i = 0; i < array.length; i++) {
            Object obj = array[i];
            if (key == null && obj == null || key != null && key.equals(obj)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean equals(Object value1, Object value2) {
        return value1 == null && value2 == null || value1 != null && value1.equals(value2);
    }

    public static String toShortLocaleTimeString(long time, Locale locale) {
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
        return timeFormat.format(new Date(time));
    }

}
