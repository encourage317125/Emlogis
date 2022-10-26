package com.emlogis.common;

import com.emlogis.model.tenant.PasswordPolicies;
import com.emlogis.model.tenant.dto.PasswordPoliciesDto;
import org.apache.commons.lang3.StringUtils;

public class PasswordUtils {

    public static final String UPPERCASE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERCASE_CHARSET = "abcdefghijklmnopqrstuvwxyz";
    public static final String NUMBER_CHARSET = "0123456789";
    public static final String NON_ALPHA_CHARSET = "-+=/<>[]{}()!?";

    public static String getPasswordViolations(PasswordPoliciesDto policiesDto, String password) {
        String violations = "";
        if (policiesDto.getMinPasswordLength() >= 0 && password.length() < policiesDto.getMinPasswordLength()) {
            violations += "MinPasswordLength,";
        }
        if (policiesDto.getMaxPasswordLength() >= 0 && password.length() >= policiesDto.getMaxPasswordLength()) {
            violations += "MaxPasswordLength,";
        }
        if (policiesDto.isRequireAtLeastOneUppercaseChar()) {
            if (!StringUtils.containsAny(password, UPPERCASE_CHARSET)) {
                violations += "AtLeastOneUppercaseChar,";
            }
        }
        if (policiesDto.isRequireAtLeastOneLowercaseChar()) {
            if (!StringUtils.containsAny(password, LOWERCASE_CHARSET)) {
                violations += "AtLeastOneUppercaseChar,";
            }
        }
        if (policiesDto.isRequireAtLeastOneNumberChar()) {
            if (!StringUtils.containsAny(password, NUMBER_CHARSET)) {
                violations += "AtLeastOneNumberChar,";
            }
        }
        if (policiesDto.isRequireAtLeastOneNonalphaChar()) {
            if (!StringUtils.containsAny(password, NON_ALPHA_CHARSET)) {
                violations += "AtLeastOneNonalphaChar,";
            }
        }
        violations = StringUtils.isNotEmpty(violations) ? violations.substring(0, violations.length() - 1) : violations;
        return violations;
    }

    public static String getPasswordViolations(PasswordPolicies policies, String password) {
        String violations = "";
        if (policies.getMinPasswordLength() >= 0 && password.length() < policies.getMinPasswordLength()) {
            violations += "MinPasswordLength,";
        }
        if (policies.getMaxPasswordLength() >= 0 && password.length() > policies.getMaxPasswordLength()) {
            violations += "MaxPasswordLength,";
        }
        if (policies.isRequireAtLeastOneUppercaseChar()) {
            if (!StringUtils.containsAny(password, UPPERCASE_CHARSET)) {
                violations += "AtLeastOneUppercaseChar,";
            }
        }
        if (policies.isRequireAtLeastOneLowercaseChar()) {
            if (!StringUtils.containsAny(password, LOWERCASE_CHARSET)) {
                violations += "AtLeastOneUppercaseChar,";
            }
        }
        if (policies.isRequireAtLeastOneNumberChar()) {
            if (!StringUtils.containsAny(password, NUMBER_CHARSET)) {
                violations += "AtLeastOneNumberChar,";
            }
        }
        if (policies.isRequireAtLeastOneNonalphaChar()) {
            if (!StringUtils.containsAny(password, NON_ALPHA_CHARSET)) {
                violations += "AtLeastOneNonalphaChar,";
            }
        }
        violations = StringUtils.isNotEmpty(violations) ? violations.substring(0, violations.length() - 1) : violations;
        return violations;
    }

    public static String generatePassword(PasswordPolicies policies) {
        return new PasswordGenerator().generatePassword(policies);
    }

}
