package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;

public class NumericValidator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        Object valueObj = validationObject.getValue();
        boolean passNull = (Boolean) validationObject.getValueByName(Constants.PASS_NULL);

        if (valueObj == null && passNull) {
            return true;
        }

        Long min = (Long) validationObject.getValueByName(Constants.MIN);
        Long max = (Long) validationObject.getValueByName(Constants.MAX);

        try {
            Long value = valueObj == null ? 0 : (Long) valueObj;
            return value >= min && value <= max;
        } catch (ClassCastException e) {
            throw new ValidationException(String.format("Can't validate %s", valueObj), e);
        }
    }
}
