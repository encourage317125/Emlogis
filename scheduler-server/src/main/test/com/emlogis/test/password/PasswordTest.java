package com.emlogis.test.password;

import com.emlogis.common.PasswordUtils;
import com.emlogis.model.tenant.PasswordPolicies;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PasswordTest {

    private PasswordPolicies passwordPolicies;

    @Before
    public void createPasswordPolicies() {
        passwordPolicies = new PasswordPolicies();

        passwordPolicies.setMinPasswordLength(6);
        passwordPolicies.setMaxPasswordLength(16);
        passwordPolicies.setRequireAtLeastOneLowercaseChar(true);
        passwordPolicies.setRequireAtLeastOneNonalphaChar(true);
    }

    @Test
    public void testPasswordGenerations() {
        for (int i = 0; i < 30; i++) {
            String password = PasswordUtils.generatePassword(passwordPolicies);
            String violations = PasswordUtils.getPasswordViolations(passwordPolicies, password);
            System.out.println(password + " " + violations);
            Assert.assertTrue(StringUtils.isEmpty(violations));
        }
    }

}
