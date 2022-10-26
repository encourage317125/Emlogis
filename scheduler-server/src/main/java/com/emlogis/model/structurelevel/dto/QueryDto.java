package com.emlogis.model.structurelevel.dto;

import java.io.Serializable;
import java.util.Collection;

public class QueryDto implements Serializable {

    private String id;

    private String name;

    private String clName;

    private Collection<QueryDto> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClName() {
        return clName;
    }

    public void setClName(String clName) {
        this.clName = clName;
    }

    public Collection<QueryDto> getChildren() {
        return children;
    }

    public void setChildren(Collection<QueryDto> children) {
        this.children = children;
    }
}
