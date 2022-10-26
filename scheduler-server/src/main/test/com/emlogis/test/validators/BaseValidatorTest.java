package com.emlogis.test.validators;

import com.emlogis.common.validation.MessageResourceLocator;
import com.emlogis.common.validation.Validator;
import com.emlogis.rest.security.SessionService;
import org.mockito.Mockito;

public abstract class BaseValidatorTest {

    private Validator validator;

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;

        if (validator instanceof MessageResourceLocator) {
            SessionService sessionService = Mockito.mock(SessionService.class);
            Mockito.when(sessionService.getMessage(Mockito.anyString(), Mockito.any())).thenReturn("no message");

            ((MessageResourceLocator) validator).setSessionService(sessionService);
        }
    }
}
