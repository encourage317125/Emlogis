package com.emlogis.model.schedule;

public enum AssignmentType {
    ENGINE("Engine"),
    MANUAL("Manual");

    private String value;

    private AssignmentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
