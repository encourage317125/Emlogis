package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.BaseEntityDto;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Field;

@Stateless
@LocalBean
@EJB(name = "EntityExistValidator", beanInterface = EntityExistValidatorLocal.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EntityExistValidatorBean implements EntityExistValidatorLocal {

	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Override
    public boolean validate(ValidationObject validationObject) {
        Object key;
        Object value = validationObject.getValue();
        Class type = validationObject.getType();
        if (!(value instanceof PrimaryKey || value instanceof String || value instanceof Number)) {
            Field field = EmlogisUtils.searchAnnotatedField(type, Id.class);
            try {
                if (value.getClass() == type) {
                    field.setAccessible(true);
                    key = field.get(value);
                } else if (value instanceof BaseEntityDto) {
                    Field dtoField = value.getClass().getDeclaredField(field.getName());
                    dtoField.setAccessible(true);
                    key = dtoField.get(value);
                } else {
                    key = value;
                }
            } catch (Exception e) {
                key = value;
            }
        } else {
            key = value;
        }

        Object entity = entityManager.find(validationObject.getType(), key);
        return entity != null;
    }
}
