package com.emlogis.server.services;

import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class PasswordCoder extends DefaultCoder {

    @Override
    public String decode(String text) {
        return text;
    }

    @Override
    public String encode(String text) {
        return text;
    }
}
