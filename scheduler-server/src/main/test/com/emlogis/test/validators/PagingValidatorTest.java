package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.PagingValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PagingValidatorTest extends BaseValidatorTest {

    @Before
    public void before() {
        setValidator(new PagingValidator());
    }

    @Test
    public void testValidation() {
        ValidationObject validationObject = new ValidationObject();

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);

        validationObject.addNamedValue(Constants.ORDER_BY, "id");
        valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);

        validationObject.addNamedValue(Constants.ORDER_DIR, "asc");
        valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);

        validationObject.addNamedValue(Constants.ORDER_DIR, "DeSc");
        valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test(expected = ValidationException.class)
    public void testWrongOrderDir() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.ORDER_BY, "id");
        validationObject.addNamedValue(Constants.ORDER_DIR, "Wrong Direction");
        getValidator().validate(validationObject);
    }

    @Test(expected = ValidationException.class)
    public void testWrongOrderBy() {
        ValidationObject validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.ORDER_DIR, "DESC");
        getValidator().validate(validationObject);
    }
}
