package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.NumericValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NumericValidatorTest extends BaseValidatorTest {

    @Before
    public void before() {
        setValidator(new NumericValidator());
    }

    @Test
    public void testValidation() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.MAX, 1000L);
        validationObject.addNamedValue(Constants.MIN, 100L);
        validationObject.addNamedValue(Constants.PASS_NULL, false);

        validationObject.addDefaultValue(200L);

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test
    public void testWrongBounds() {
        ValidationObject validationObject = new ValidationObject();
        validationObject.addNamedValue(Constants.MAX, 1000L);
        validationObject.addNamedValue(Constants.MIN, 100L);
        validationObject.addNamedValue(Constants.PASS_NULL, false);

        validationObject.addDefaultValue(1200L);

        boolean valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);

        validationObject.addDefaultValue(10L);

        valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }
}
