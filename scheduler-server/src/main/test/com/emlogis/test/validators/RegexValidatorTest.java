package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.RegexValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegexValidatorTest extends BaseValidatorTest {

    @Before
    public void before() {
        setValidator(new RegexValidator());
    }

    @Test
    public void testValidation() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.REGEX, Constants.EMAIL_REGEX);
        validationObject.addNamedValue(Constants.CASE_SENSITIVE, false);

        validationObject.addDefaultValue("correct@email.com");

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test
    public void testWrongValidation() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.REGEX, Constants.EMAIL_REGEX);
        validationObject.addNamedValue(Constants.CASE_SENSITIVE, false);

        validationObject.addDefaultValue("incorrect@email");

        boolean valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }
}
