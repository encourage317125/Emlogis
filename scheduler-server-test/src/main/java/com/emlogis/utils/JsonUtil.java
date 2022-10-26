package com.emlogis.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.io.*;

/**
 * Created by user on 02.10.15.
 */
public class JsonUtil {

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
}
