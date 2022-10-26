package com.emlogis.ihub.security;

/**
 * Created by Andrii Mozharovskyi on 10.07.2015.
 */
import javax.servlet.http.*;

import com.actuate.iportal.security.iPortalSecurityAdapter;
import com.emlogis.ihub.security.auth.IAuthenticationValidator;
import com.emlogis.ihub.security.auth.impl.MercuryAuthenticationValidator;
import com.emlogis.ihub.security.utils.PropertyUtil;
import com.emlogis.ihub.security.utils.logging.Logger;
import com.emlogis.ihub.security.utils.logging.impl.LoggerFactory;
import java.util.Date;

public class EmlogisIhubSecurityAdapter extends iPortalSecurityAdapter {
    private String volumeProfile = PropertyUtil.get("volume_profile");
    private String userName = null;
    private String password = null;

    public Logger logger = LoggerFactory.getInstance();

    public static void main(String[] args) {
        try {
            EmlogisIhubSecurityAdapter securityAdapter = new EmlogisIhubSecurityAdapter();
            boolean isAuth = securityAdapter.isAuthenticated("cff22853-7530-4ab3-9eda-49fa98c94d8b");
            System.out.println(PropertyUtil.get("auth_validation_url"));
            securityAdapter.logger.log(isAuth+"");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public EmlogisIhubSecurityAdapter() {
        logger.setDebug(Boolean.valueOf(PropertyUtil.get("debugging-logs")));
        if(Boolean.valueOf(PropertyUtil.get("clear_logs_on_server_restart"))) {
            logger.clear();
        }
        logger.log(new Date().toString());
    }

    public boolean authenticate(HttpServletRequest httpservletrequest) {
        String ADMIN_USR = PropertyUtil.get("admin_username");
        String ADMIN_PWD = PropertyUtil.get("admin_password");
        String securityTokenName = PropertyUtil.get("security_token_parameter_name");

        String param = httpservletrequest.getParameter(securityTokenName);
        boolean secured = true;
        try {

            String userid = httpservletrequest.getParameter("userid");
            String password = httpservletrequest.getParameter("password");

            logger.log("Code = [" + param + "]");
            logger.log("useridPar = [" + userid + "]");
            logger.log("passwordPar = [" + password + "]");

            if(ADMIN_USR.equalsIgnoreCase(userid) && ADMIN_PWD.equals(password)) {
                logger.log("Authenticated as administrator");
                setUserName(ADMIN_USR);
                setPassword(ADMIN_PWD);
                return true;
            }

            if (isAuthenticated(param)) {
                logger.log("Authenticated as user!");
                setUserName(ADMIN_USR);
                setPassword(ADMIN_PWD);
            } else {
                logger.log("NOT Authenticated");
                secured = false;
            }
        } catch(Throwable t) {
            secured = false;
            logger.err(t);
        }
        finally {
            logger.log("Secured = [" + secured + "]");
        }

        return secured;
    }

    public boolean isAuthenticated(String securityCode) {
        if(securityCode == null || securityCode.equals("")) {
            return false;
        }
        IAuthenticationValidator validator = new MercuryAuthenticationValidator();
        return validator.isAuthenticated(securityCode);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getVolumeProfile() {
        return volumeProfile;
    }
}