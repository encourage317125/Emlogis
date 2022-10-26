package com.emlogis.model.common;

/**
 * Created by alexander.borlis on 3/18/14.
 */
public enum ChangeType {

    PERSIST(1, "Create"),
    UPDATE(2, "Update"),
    REMOVE(3, "Delete");

    private final int id;
    private final String type;

    ChangeType(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public long id() {
        return id;
    }

    public String type() {
        return type;
    }
}
