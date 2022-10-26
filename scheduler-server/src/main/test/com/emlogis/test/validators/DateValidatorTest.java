package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.DateValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateValidatorTest extends BaseValidatorTest {

    private final static String TEST_DATE_AFTER = "1910/01/01 11:05:40"; // use for date after
    private final static String TEST_DATE_BEFORE = "2100/01/01 01:25:45"; // use for date before
    private final static String TEST_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; // use for date before

    private ValidationObject validationObject;

    @Before
    public void before() {
        setValidator(new DateValidator());

        validationObject = new ValidationObject();

        validationObject.addNamedValue(Constants.DATE_BEFORE, TEST_DATE_BEFORE);
        validationObject.addNamedValue(Constants.DATE_AFTER, TEST_DATE_AFTER);
        validationObject.addNamedValue(Constants.DATE_PATTERN, TEST_DATE_FORMAT);
        validationObject.addNamedValue(Constants.PASS_NULL, false);
    }

    @Test
    public void testValidation() {
        validationObject.addDefaultValue(new Date().getTime());

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test
    public void testWrongDateBounds() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 1909);

        validationObject.addDefaultValue(calendar.getTimeInMillis());

        boolean valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);

        calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2101);

        validationObject.addDefaultValue(calendar.getTimeInMillis());

        valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }

}
