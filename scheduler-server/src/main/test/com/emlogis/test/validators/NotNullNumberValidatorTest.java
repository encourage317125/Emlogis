package com.emlogis.test.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.validators.NotNullNumberValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NotNullNumberValidatorTest extends BaseValidatorTest {

    private ValidationObject validationObject;

    class FullName {
        String firstName;
        String secondName;
        String middleName;
        String nickName;
        Address address;
    }
    class Address {
        String district;
        String street;
        String number;
    }

    @Before
    public void before() {
        setValidator(new NotNullNumberValidator());

        validationObject = new ValidationObject();
    }

    @Test
    public void testValidation() {
        FullName fullName = new FullName();
        fullName.firstName = "Joe";
        fullName.nickName = "Joe";

        validationObject.addNamedValue(Constants.FIELD_NAMES,
                new String[] {"firstName", "secondName", "middleName", "nickName"});
        validationObject.addNamedValue(Constants.NOT_NULL_NUMBER, 2);

        validationObject.addDefaultValue(fullName);

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test
    public void testWrongValidation() {
        FullName fullName = new FullName();
        fullName.firstName = "Joe";

        validationObject.addNamedValue(Constants.FIELD_NAMES,
                new String[] {"firstName", "secondName", "middleName"});
        validationObject.addNamedValue(Constants.NOT_NULL_NUMBER, 2);

        validationObject.addDefaultValue(fullName);

        boolean valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }

    @Test
    public void testPathValidation() {
        FullName fullName = new FullName();
        fullName.address = new Address();
        fullName.address.district = "Region";

        validationObject.addNamedValue(Constants.FIELD_NAMES,
                new String[] {"address.street", "address.district"});
        validationObject.addNamedValue(Constants.NOT_NULL_NUMBER, 1);

        validationObject.addDefaultValue(fullName);

        boolean valid = getValidator().validate(validationObject);
        Assert.assertTrue(valid);
    }

    @Test
    public void testWrongPathValidation() {
        FullName fullName = new FullName();

        validationObject.addNamedValue(Constants.FIELD_NAMES,
                new String[] {"address.street", "address.district"});
        validationObject.addNamedValue(Constants.NOT_NULL_NUMBER, 1);

        validationObject.addDefaultValue(fullName);

        boolean valid = getValidator().validate(validationObject);
        Assert.assertFalse(valid);
    }

}
