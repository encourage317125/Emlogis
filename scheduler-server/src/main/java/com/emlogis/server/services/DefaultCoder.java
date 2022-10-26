package com.emlogis.server.services;

import org.apache.commons.codec.binary.Base64;

public class DefaultCoder {

    public String encode(String text) {
        String result = null;
        if (text != null) {
            result = new String(Base64.encodeBase64(text.getBytes()));
        }
        return result;
    }

    public String decode(String text) {
        String result = null;
        if (text != null) {
            result = new String(Base64.encodeBase64(text.getBytes()));
        }
        return result;
    }

}
