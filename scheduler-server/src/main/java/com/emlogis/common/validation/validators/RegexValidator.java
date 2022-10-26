package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexValidator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        String regex = (String) validationObject.getValueByName(Constants.REGEX);
        boolean caseSensitive = (Boolean) validationObject.getValueByName(Constants.CASE_SENSITIVE);

        if (validationObject.getValue() != null) {
            String value = validationObject.getValue().toString();

            Pattern pattern;
            if (caseSensitive) {
                pattern = Pattern.compile(regex);
            } else {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            }
            Matcher matcher = pattern.matcher(value);

            return matcher.matches();
        } else {
            return true;
        }
    }
}
