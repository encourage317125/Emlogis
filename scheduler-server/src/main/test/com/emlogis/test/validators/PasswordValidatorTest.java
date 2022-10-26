package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.PasswordValidator;
import com.emlogis.model.tenant.PasswordPolicies;
import com.emlogis.model.tenant.Tenant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PasswordValidatorTest extends BaseValidatorTest {

    @Mock
    TenantService tenantServiceMock;

    @Mock
    ResourcesBundle resourcesBundleMock;

    @Before
    public void before() {
        setValidator(new PasswordValidator());

        PasswordPolicies passwordPolicies = new PasswordPolicies();
        Mockito.when(tenantServiceMock.getPasswordPolicies(Mockito.anyString())).thenReturn(passwordPolicies);
        Mockito.when(tenantServiceMock.getTenant(Mockito.anyString())).thenReturn(new Tenant());

        Mockito.when(resourcesBundleMock.getMessage(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn("Password is not valid");
    }

    @Test
    public void testValidation() {
        ValidationObject validationObject = new ValidationObject();
        validationObject.addNamedValue(Constants.TENANT_SERVICE, tenantServiceMock);
        validationObject.addNamedValue(Constants.RESOURCES_BUNDLE, resourcesBundleMock);
        validationObject.addNamedValue(Constants.TENANT_ID, "test");
        validationObject.addNamedValue(Constants.PASSWORD, "password");

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test(expected = ValidationException.class)
    public void testWrongPassword() {
        ValidationObject validationObject = new ValidationObject();
        validationObject.addNamedValue(Constants.TENANT_SERVICE, tenantServiceMock);
        validationObject.addNamedValue(Constants.RESOURCES_BUNDLE, resourcesBundleMock);
        validationObject.addNamedValue(Constants.TENANT_ID, "test");
        validationObject.addNamedValue(Constants.PASSWORD, "no");

        getValidator().validate(validationObject);
    }

}
