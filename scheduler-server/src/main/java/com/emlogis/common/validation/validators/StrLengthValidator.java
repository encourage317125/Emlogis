package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;

public class StrLengthValidator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        String value = (String) validationObject.getValue();
        boolean passNull = (Boolean) validationObject.getValueByName(Constants.PASS_NULL);

        if (value == null && passNull) {
            return true;
        }

        Integer min = (Integer) validationObject.getValueByName(Constants.MIN);
        Integer max = (Integer) validationObject.getValueByName(Constants.MAX);

        int length = value == null ? 0 : value.length();

        return length >= min && length <= max;
    }
}
