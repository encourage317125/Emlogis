package com.emlogis.ihub.security.utils.logging.impl;

import com.emlogis.ihub.security.utils.PropertyUtil;
import com.emlogis.ihub.security.utils.logging.Logger;

import java.io.*;

/**
 * Created by Andrii Mozharovskyi on 10.07.2015.
 */
public class CustomFileLogger extends Logger {

    private static final String LOGGING_FILE_PATH = PropertyUtil.get("logging_file_path");
    private static final String FILE_NAME = PropertyUtil.get("logging_file_name");

    private static volatile CustomFileLogger instance;
    public static CustomFileLogger getInstance() {
        CustomFileLogger localInstance = instance;
        if (localInstance == null) {
            synchronized (CustomFileLogger.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new CustomFileLogger();
                }
            }
        }
        return localInstance;
    }

    private CustomFileLogger() { }

    @Override
    public void log(String message) {
        if(isDebug()){
            try {
                PrintWriter out = getWriter(true);
                out.println(message);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void err(Throwable t) {
        boolean deb = isDebug();
        setDebug(true);
        log("--- ERROR stack trace --- " + t.toString());
        for(StackTraceElement s : t.getStackTrace()) {
            log("     " + s);
        }
        log("--- ERROR stack trace END ---");
        setDebug(deb);
    }

    @Override
    public void clear() {
        if(isDebug()){
            try {
                PrintWriter out = getWriter(false);
                out.print("");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private PrintWriter getWriter(boolean edit) throws IOException {
        //return new PrintWriter(new BufferedWriter(new FileWriter(getCurrentWorkingDir() + "/" + FILE_NAME, edit)));
        return new PrintWriter(new BufferedWriter(new FileWriter((LOGGING_FILE_PATH.equals("") ?
                getCurrentWorkingDir()  : LOGGING_FILE_PATH ) + "/" + FILE_NAME, edit)));
    }

    private String getCurrentWorkingDir() {
        return System.getProperty("user.dir");
    }
}
