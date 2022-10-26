package com.emlogis.common.validation;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.*;
import com.emlogis.model.PrimaryKey;
import com.emlogis.rest.security.SessionService;
import com.emlogis.server.services.PasswordCoder;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Validation
@Interceptor
public class ValidationInterceptor {

    @EJB
    private TenantService tenantService;

    @EJB
    private ResourcesBundle resourcesBundle;

    @EJB
    private ValidatorHelper helper;

    @EJB
    private PasswordCoder passwordCoder;

    @EJB
    private SessionService sessionService;

    interface ValidationObjectCreator<A extends Annotation> {
        ValidationObject create(A annotation, Object parameter) throws IllegalAccessException;
    }

    interface ParameterHandler {
        <A extends Annotation, V extends Validator> void handle(
                Annotation[] annotations,
                Object parameter,
                Class<A> annotationClass,
                Class<V> validatorClass,
                ValidationObjectCreator<A> creator) throws IllegalAccessException;
    }

    @AroundInvoke
    public Object validate(InvocationContext context) throws Exception {
        ValidationExceptionBuilder validationExceptionBuilder = new ValidationExceptionBuilder();
        validationExceptionBuilder.setSessionService(sessionService);

        Method method = context.getMethod();

        Object[] parameters = context.getParameters();

        Map<Class<? extends Validator>, List<ValidationObject>> classMap = getParamValidators(method, parameters);

        Set<Class<? extends Validator>> classes = classMap.keySet();
        for (Class<? extends Validator> clazz : classes) {
            Validator validator = getValidator(clazz);

            List<ValidationObject> validationObjectList = classMap.get(clazz);

            for (ValidationObject validationObject : validationObjectList) {
                boolean validated = validator.validate(validationObject) == validationObject.isExpectedResult();

                if (!validated) {
                    Map<String, Object> paramMap = validationObject.getValueMap();
                    if (validationObject.getType() != null) {
                        if (paramMap == null) {
                            paramMap = new HashMap<>();
                        }
                        paramMap.put(Constants.VALIDATION_OBJECT_TYPE, validationObject.getType());
                    }
                    ValidationException exception = validationExceptionBuilder.build(paramMap, clazz);
                    if (exception == null) {
                        String message = sessionService.getMessage(Constants.VALIDATION_VALIDATOR_FAILED, clazz,
                                EmlogisUtils.toJsonString(paramMap));
                        exception = new ValidationException(message);
                    }
                    throw exception;
                }
            }
        }

        return context.proceed();
    }

    private Validator getValidator(Class<? extends Validator> clazz) throws InstantiationException,
            IllegalAccessException {
        Validator result;

        Annotation annotation = clazz.getAnnotation(EJB.class);
        if (annotation != null) {
            SessionContext sessionContext = helper.getContext();
            String name = ((EJB) annotation).name();
            result = (Validator) sessionContext.lookup(name);
        } else {
            result = clazz.newInstance();
            if (result instanceof MessageResourceLocator) {
                ((MessageResourceLocator) result).setSessionService(sessionService);
            }
        }

        return result;
    }

    private Map<Class<? extends Validator>, List<ValidationObject>> getParamValidators(
            Method method, final Object[] parameters) throws IllegalAccessException {
        final Map<Class<? extends Validator>, List<ValidationObject>> result = new HashMap<>();

        ParameterHandler handler = new ParameterHandler() {
            @Override
            public <A extends Annotation, V extends Validator> void handle(
                    Annotation[] annotations,
                    Object parameter,
                    Class<A> annotationClass,
                    Class<V> validatorClass,
                    ValidationObjectCreator<A> creator) throws IllegalAccessException {
                List<A> extractedAnnotations = extractAnnotations(annotations, annotationClass);
                for (A annotation : extractedAnnotations) {
                    List<ValidationObject> validationObjectList = result.get(validatorClass);
                    if (validationObjectList == null) {
                        validationObjectList = new ArrayList<>();
                        result.put(validatorClass, validationObjectList);
                    }
                    ValidationObject validationObject = creator.create(annotation, parameter);
                    validationObjectList.add(validationObject);
                }
            }
        };

        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            final Object parameter = parameters[i];

            if (parameter != null) {
                // Validate fields of incoming parameters
                Class clazz = parameter.getClass();
                final List<Field> fields = EmlogisUtils.getAllFields(clazz);

                handler.handle(annotations, parameter, ValidateStrLength.class, StrLengthValidator.class,
                        new ValidationObjectCreator<ValidateStrLength>() {
                            @Override
                            public ValidationObject create(ValidateStrLength annotation, Object parameter)
                                    throws IllegalAccessException {
                                ValidationObject result = new ValidationObject();

                                result.addNamedValue(Constants.MIN, annotation.min());
                                result.addNamedValue(Constants.MAX, annotation.max());
                                result.addNamedValue(Constants.PASS_NULL, annotation.passNull());
                                result.addNamedValue(Constants.FIELD_NAME, annotation.field());

                                Object value = EmlogisUtils.getPathFieldValue(annotation.field(), parameter);
                                result.addDefaultValue(value);

                                return result;
                            }
                        });

                handler.handle(annotations, parameter, ValidateNumeric.class, NumericValidator.class,
                        new ValidationObjectCreator<ValidateNumeric>() {
                            @Override
                            public ValidationObject create(ValidateNumeric annotation, Object parameter)
                                    throws IllegalAccessException {
                                ValidationObject result = new ValidationObject();

                                result.addNamedValue(Constants.MIN, annotation.min());
                                result.addNamedValue(Constants.MAX, annotation.max());
                                result.addNamedValue(Constants.PASS_NULL, annotation.passNull());
                                result.addNamedValue(Constants.FIELD_NAME, annotation.field());

                                Object value = getFieldValue(fields, annotation.field(), parameter);
                                result.addDefaultValue(value);

                                return result;
                            }
                        });

                handler.handle(annotations, parameter, ValidateDate.class, DateValidator.class,
                        new ValidationObjectCreator<ValidateDate>() {
                            @Override
                            public ValidationObject create(ValidateDate annotation, Object parameter)
                                    throws IllegalAccessException {
                                ValidationObject result = new ValidationObject();

                                result.addNamedValue(Constants.DATE_BEFORE, annotation.before());
                                result.addNamedValue(Constants.DATE_AFTER, annotation.after());
                                result.addNamedValue(Constants.DATE_PATTERN, annotation.format());
                                result.addNamedValue(Constants.PASS_NULL, annotation.passNull());
                                result.addNamedValue(Constants.FIELD_NAME, annotation.field());

                                Object value = getFieldValue(fields, annotation.field(), parameter);
                                result.addDefaultValue(value);

                                return result;
                            }
                        });

                handler.handle(annotations, parameter, ValidateRegex.class, RegexValidator.class,
                        new ValidationObjectCreator<ValidateRegex>() {
                            @Override
                            public ValidationObject create(ValidateRegex annotation, Object parameter)
                                    throws IllegalAccessException {
                                ValidationObject result = new ValidationObject();

                                result.addNamedValue(Constants.REGEX, annotation.regex());
                                result.addNamedValue(Constants.CASE_SENSITIVE, annotation.caseSensitive());
                                result.addNamedValue(Constants.FIELD_NAME, annotation.field());

                                Object value = EmlogisUtils.getPathFieldValue(annotation.field(), parameter);
                                result.addDefaultValue(value);

                                return result;
                            }
                        });

                handler.handle(annotations, parameter, ValidateNotNullNumber.class, NotNullNumberValidator.class,
                        new ValidationObjectCreator<ValidateNotNullNumber>() {
                            @Override
                            public ValidationObject create(ValidateNotNullNumber annotation, Object parameter) {
                                ValidationObject result = new ValidationObject();

                                result.addNamedValue(Constants.FIELD_NAMES, annotation.fields());
                                result.addNamedValue(Constants.NOT_NULL_NUMBER, annotation.minNotNulls());

                                result.addDefaultValue(parameter);

                                return result;
                            }
                        });

                handler.handle(annotations, parameter, ValidatePassword.class, PasswordValidator.class,
                        new ValidationObjectCreator<ValidatePassword>() {
                            @Override
                            public ValidationObject create(ValidatePassword annotation, Object parameter)
                                    throws IllegalAccessException {
                                ValidationObject result = new ValidationObject();

                                String tenantField = annotation.tenantField();
                                Object tenantId;
                                if (StringUtils.isNotEmpty(tenantField)) {
                                    tenantId = getFieldValue(fields, tenantField, parameter);
                                } else {
                                    tenantId = sessionService.getTenantId();
                                }
                                Object password = getFieldValue(fields, annotation.passwordField(), parameter);
                                String decodedPassword = passwordCoder.decode((String) password);

                                result.addNamedValue(Constants.TENANT_SERVICE, tenantService);
                                result.addNamedValue(Constants.RESOURCES_BUNDLE, resourcesBundle);
                                result.addNamedValue(Constants.TENANT_ID, tenantId);
                                result.addNamedValue(Constants.PASSWORD, decodedPassword);

                                return result;
                            }
                        });

                handler.handle(annotations, parameter, ValidateUnique.class, UniqueValidatorBean.class,
                        new ValidationObjectCreator<ValidateUnique>() {
                            @Override
                            public ValidationObject create(ValidateUnique annotation, Object parameter)
                                    throws IllegalAccessException {
                                ValidationObject result = new ValidationObject();

                                result.setType(annotation.type());
                                result.addNamedValue(Constants.FIELD_NAMES, getRefinedFields(annotation.fields()));
                                if (!annotation.globally()) {
                                    String tenantId = sessionService.getTenantId();
                                    result.addNamedValue(Constants.TENANT_ID, tenantId);
                                }

                                Object idValue = null;
                                if (isNotBean(parameter) && annotation.fields().length == 1) {
                                    result.addDefaultValue(parameter);
                                } else {
                                    Object[] value = getFieldValues(fields, annotation.fields(), parameter);
                                    result.addDefaultValue(value);

                                    Field id = EmlogisUtils.findFieldByName(fields, Constants.ID);
                                    if (id != null) {
                                        id.setAccessible(true);
                                        idValue = id.get(parameter);
                                        result.addNamedValue(Constants.ID, idValue);
                                    }
                                }
                                if (idValue == null) {
                                    // find primaryKey between parameters
                                    for (int j = 0; j < parameters.length; j++) {
                                        Object param = parameters[j];
                                        if (param instanceof PrimaryKey) {
                                            // make sure that this is primary key of updating/creating DTO
                                            Annotation[] keyAnnotations = parameterAnnotations[j];
                                            List<Validate> validateAnnotations
                                                    = extractAnnotations(keyAnnotations, Validate.class);
                                            for (Validate validateAnnotation : validateAnnotations) {
                                                if (validateAnnotation.validator() == EntityExistValidatorBean.class) {
                                                    if (validateAnnotation.type() == annotation.type()) {
                                                        idValue = ((PrimaryKey) param).getId();
                                                        result.addNamedValue(Constants.ID, idValue);
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                        if (idValue != null) {
                                            break;
                                        }
                                    }
                                }

                                return result;
                            }
                        });

            }

            List<Validate> validateAnnotations = extractAnnotations(annotations, Validate.class);
            for (Validate validateAnnotation : validateAnnotations) {
                Class<? extends Validator> validatorClass = validateAnnotation.validator();
                List<ValidationObject> validationObjectList = result.get(validatorClass);
                if (validationObjectList == null) {
                    validationObjectList = new ArrayList<>();
                    result.put(validatorClass, validationObjectList);
                }

                ValidationObject validationObject;
                String group = validateAnnotation.group();
                if (StringUtils.isEmpty(group)) {
                    validationObject = new ValidationObject(validateAnnotation.expectedResult(),
                            validateAnnotation.type());
                    validationObjectList.add(validationObject);
                } else {
                    validationObject = searchGroupValidationObject(validationObjectList, group);
                    if (validationObject == null) {
                        validationObject = new ValidationObject(validateAnnotation.expectedResult(),
                                validateAnnotation.type(), group);
                        validationObjectList.add(validationObject);
                    }
                }
                validationObject.addNamedValue(validateAnnotation.name(), parameter);
            }

            for (Annotation annotation : annotations) {
                if (annotation instanceof ValidatePaging) {
                    Class<? extends Validator> validatorClass = PagingValidator.class;

                    ValidationObject validationObject;

                    List<ValidationObject> validationObjectList = result.get(validatorClass);
                    if (validationObjectList == null) {
                        validationObjectList = new ArrayList<>();
                        result.put(validatorClass, validationObjectList);

                        validationObject = new ValidationObject();
                        validationObjectList.add(validationObject);
                    } else {
                        validationObject = validationObjectList.get(0);
                    }

                    validationObject.addNamedValue(((ValidatePaging) annotation).name(), parameter);
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> List<T> extractAnnotations(Annotation[] annotations, Class<T> clazz) {
        List<T> result = new ArrayList<>();

        T[] extractedAnnotations = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof ValidateAll) {
                if (Validate.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).value();
                } else if (ValidateDate.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).dates();
                } else if (ValidateNumeric.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).numerics();
                } else if (ValidatePaging.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).pagings();
                } else if (ValidatePassword.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).passwords();
                } else if (ValidateRegex.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).regexes();
                } else if (ValidateStrLength.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).strLengths();
                } else if (ValidateUnique.class.equals(clazz)) {
                    extractedAnnotations = (T[]) ((ValidateAll) annotation).uniques();
                }
                if (extractedAnnotations != null) {
                    result.addAll(Arrays.asList(extractedAnnotations));
                }
            } else if (clazz.isInstance(annotation)) {
                result.add((T) annotation);
            }
        }

        return result;
    }

    private ValidationObject searchGroupValidationObject(List<ValidationObject> validationObjects, String group) {
        ValidationObject result = null;

        for (ValidationObject validationObject : validationObjects) {
            if (StringUtils.equals(group, validationObject.getGroup())) {
                result = validationObject;
                break;
            }
        }

        return result;
    }

    private Object[] getFieldValues(List<Field> fields, String[] names, Object instance) throws IllegalAccessException {
        Object[] result = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            result[i] = EmlogisUtils.getPathFieldValue(names[i], instance);
        }
        return result;
    }

    private Object getFieldValue(List<Field> fields, String name, Object instance) throws IllegalAccessException {
        Field field = EmlogisUtils.findFieldByName(fields, name);
        if (field != null) {
            field.setAccessible(true);
            return field.get(instance);
        } else {
            throw new RuntimeException(sessionService.getMessage("validation.wrong.configuration"));
        }
    }

    private boolean isNotBean(Object instance) {
        return instance.getClass().isPrimitive() || instance instanceof String || instance instanceof Character
                || instance instanceof Number || instance instanceof Date || instance instanceof Boolean
                || instance instanceof Collection;
    }

    private String[] getRefinedFields(String[] fields) {
        String[] result = new String[fields.length];
        for (int i = 0; i < result.length; i++) {
            int indexOf = fields[i].lastIndexOf(".");
            if (indexOf < 0) {
                result[i] = fields[i];
            } else {
                result[i] = fields[i].substring(indexOf + 1);
            }
        }
        return result;
    }
}
