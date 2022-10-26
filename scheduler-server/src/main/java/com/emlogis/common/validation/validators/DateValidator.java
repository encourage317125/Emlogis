package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        boolean passNull = (Boolean) validationObject.getValueByName(Constants.PASS_NULL);
        Object value = validationObject.getValue();
        if (value == null && passNull) {
            return true;
        }
        
        String beforeStr = (String) validationObject.getValueByName(Constants.DATE_BEFORE);
        String afterStr = (String) validationObject.getValueByName(Constants.DATE_AFTER);
        String pattern = (String) validationObject.getValueByName(Constants.DATE_PATTERN);

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        long before;
        long after;
        try {
            before = dateFormat.parse(beforeStr).getTime();
            after = dateFormat.parse(afterStr).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        long time;
        if (value instanceof String) {
            String dateStr = (String) validationObject.getValue();

            try {
                Date date = dateFormat.parse(dateStr);
                time = date.getTime();
            } catch (Exception e) {
                throw new ValidationException(String.format("Can't parse text '%s' into a date", dateStr), e);
            }
        } else if (value instanceof Number) {
            time = ((Number) value).longValue();
        } else if (value instanceof Date) {
            time = ((Date) value).getTime();
        } else {
            throw new ValidationException(String.format("Unexpected date format for value '%s'", value));
        }
        return after <= time && time <= before;
    }
}
