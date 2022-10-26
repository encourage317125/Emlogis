package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;

public class NotNullNumberValidator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        Object valueObj = validationObject.getValue();
        int notNullNumber = (Integer) validationObject.getValueByName(Constants.NOT_NULL_NUMBER);
        String[] fieldNames = (String[]) validationObject.getValueByName(Constants.FIELD_NAMES);

        int counter = 0;
        for (String fieldName : fieldNames) {
            try {
                Object value = EmlogisUtils.getPathFieldValue(fieldName, valueObj);
                if (value != null) {
                    counter++;
                    if (counter >= notNullNumber) {
                        return true;
                    }
                }
            } catch (Exception e) {
                //ignore exception
            }
        }

        return false;
    }

}
