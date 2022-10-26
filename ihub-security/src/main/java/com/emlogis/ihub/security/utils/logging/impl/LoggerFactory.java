package com.emlogis.ihub.security.utils.logging.impl;

import com.emlogis.ihub.security.utils.logging.Logger;

/**
 * Created by emlogis on 10/19/15.
 */
public class LoggerFactory {
    public static Logger getInstance(){
        return CustomFileLogger.getInstance();
    }
}
