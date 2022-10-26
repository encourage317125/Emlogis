package com.emlogis.ihub.security.utils.logging;

/**
 * Created by Andrii Mozharovskyi on 10.07.2015.
 */
public abstract class Logger {
    private boolean debug = false;

    public abstract void log(String message);

    public abstract void err(Throwable t);

    public abstract void clear();

    protected boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
