package com.emlogis.common.validation;

import java.util.HashMap;
import java.util.Map;

public class ValidationObject {

    public static final String DEFAULT_VALUE = "value";

    private boolean expectedResult = true;

    private Class type;

    private String group;

    private Map<String, Object> valueMap = new HashMap<>();

    public ValidationObject() {}

    public ValidationObject(boolean expectedResult, Class type) {
        this.expectedResult = expectedResult;
        this.type = type;
    }

    public ValidationObject(boolean expectedResult, Class type, String group) {
        this.expectedResult = expectedResult;
        this.type = type;
        this.group = group;
    }

    public boolean isExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(boolean expectedResult) {
        this.expectedResult = expectedResult;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Map<String, Object> getValueMap() {
        return valueMap;
    }

    public void addNamedValue(String name, Object value) {
        valueMap.put(name, value);
    }

    public void addDefaultValue(Object value) {
        valueMap.put(DEFAULT_VALUE, value);
    }

    public void addAllNamedValues(Map<String, Object> values) {
        valueMap.putAll(values);
    }

    public Object getValueByName(String name) {
        return valueMap.get(name);
    }

    public Object getValue() {
        return valueMap.get(DEFAULT_VALUE);
    }

    public boolean containsName(String name) {
        return valueMap.containsKey(name);
    }
}
