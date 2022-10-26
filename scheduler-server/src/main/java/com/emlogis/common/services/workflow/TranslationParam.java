package com.emlogis.common.services.workflow;

/**
 * Created by user on 22.07.15.
 */
public class TranslationParam {

    private String name;
    private String value;

    public TranslationParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

}
