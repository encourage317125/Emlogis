package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.PasswordUtils;
import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;
import com.emlogis.model.tenant.PasswordPolicies;
import com.emlogis.model.tenant.Tenant;
import org.apache.commons.lang3.StringUtils;

public class PasswordValidator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        String password = (String) validationObject.getValueByName(Constants.PASSWORD);
        String tenantId = (String) validationObject.getValueByName(Constants.TENANT_ID);
        TenantService tenantService = (TenantService) validationObject.getValueByName(Constants.TENANT_SERVICE);

        PasswordPolicies passwordPolicies = tenantService.getPasswordPolicies(tenantId);

        String violations = PasswordUtils.getPasswordViolations(passwordPolicies, password);

        if (StringUtils.isNotEmpty(violations)) {
            ResourcesBundle bundle = (ResourcesBundle) validationObject.getValueByName(Constants.RESOURCES_BUNDLE);
            Tenant tenant = tenantService.getTenant(Tenant.class, tenantId);
            String language = tenant == null ? "en" : tenant.getLanguage();
            throw new ValidationException(bundle.getMessage(language, "validation.password.error", violations));
        }

        return true;
    }

}
