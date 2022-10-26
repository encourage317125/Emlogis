package com.emlogis.scheduler.engine.communication;

import java.io.Serializable;

public class ComponentStatus implements Serializable {

    private String name;

    private String ip;

    private long updated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
