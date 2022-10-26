package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.StrLengthValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StrLengthValidatorTest extends BaseValidatorTest {

    @Before
    public void before() {
        setValidator(new StrLengthValidator());
    }

    @Test
    public void testValidation() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.MIN, 10);
        validationObject.addNamedValue(Constants.MAX, 20);
        validationObject.addNamedValue(Constants.PASS_NULL, false);
        validationObject.addDefaultValue("1234567890"); // less then 20, more then 10
        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);

        validationObject.addDefaultValue("1234567890_1234567890"); // more then 20
        valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }

    @Test
    public void testNullValidation() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.MIN, 0);
        validationObject.addNamedValue(Constants.MAX, 20);
        validationObject.addNamedValue(Constants.PASS_NULL, false);
        validationObject.addDefaultValue(null);
        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);

        validationObject.addNamedValue(Constants.MIN, 10);
        validationObject.addDefaultValue(null);
        valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }

}
