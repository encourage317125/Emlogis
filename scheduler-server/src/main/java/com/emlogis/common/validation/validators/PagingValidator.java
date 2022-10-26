package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.validation.MessageResourceLocator;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.common.validation.Validator;
import org.apache.commons.lang3.StringUtils;

public class PagingValidator extends MessageResourceLocator implements Validator {

    @Override
    public boolean validate(ValidationObject validationObject) {
        String orderBy = (String) validationObject.getValueByName(Constants.ORDER_BY);
        String orderDir = (String) validationObject.getValueByName(Constants.ORDER_DIR);

        if (StringUtils.isNotEmpty(orderDir) && !(StringUtils.equalsIgnoreCase("ASC", orderDir)
                || StringUtils.equalsIgnoreCase("DESC", orderDir))) {
            ValidationException validationException = new ValidationException(
                    getMessage("validation.paging.orderdir.error"));
            validationException.putParamValue(Constants.ORDER_BY, orderBy);
            validationException.putParamValue(Constants.ORDER_DIR, orderDir);

            throw validationException;
        }

        if (StringUtils.isEmpty(orderBy) && StringUtils.isNotEmpty(orderDir)) {
            ValidationException validationException = new ValidationException(
                    getMessage("validation.paging.orderby.error"));
            validationException.putParamValue(Constants.ORDER_BY, orderBy);
            validationException.putParamValue(Constants.ORDER_DIR, orderDir);

            throw validationException;
        }

        return true;
    }
}
