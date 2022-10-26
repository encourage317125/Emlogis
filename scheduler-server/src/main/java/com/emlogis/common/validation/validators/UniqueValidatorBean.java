package com.emlogis.common.validation.validators;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.validation.ValidationObject;
import com.emlogis.model.tenant.Tenant;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.List;

@Stateless
@LocalBean
@EJB(name = "UniqueValidator", beanInterface = UniqueValidatorLocal.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class UniqueValidatorBean implements UniqueValidatorLocal {

	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @Override
    public boolean validate(ValidationObject validationObject) {
        String id = (String) validationObject.getValueByName(Constants.ID);
        String[] fieldNames = (String[]) validationObject.getValueByName(Constants.FIELD_NAMES);
        String type = validationObject.getType().getSimpleName();
        Object[] values = (Object[]) validationObject.getValue();
        String tenantId = null;
        if (!Tenant.class.isAssignableFrom(validationObject.getType())) {
            tenantId = (String) validationObject.getValueByName(Constants.TENANT_ID);
        }

        String excludeClosure;
        if (id != null && tenantId != null) {
            // then we need to exclude current entity from the search result
            excludeClosure = " AND NOT (t.primaryKey.id = :id AND t.primaryKey.tenantId = :tenantId)";
        } else {
            // exclude nothing
            excludeClosure = "";
        }

        Query query;
        String queryStr = "SELECT t FROM " + type + " t WHERE ";
        if (tenantId == null) { // Check globally
            for (int i = 0; i < fieldNames.length; i++) {
                queryStr += (i == 0 ? "" : " AND ") + fieldNames[i] + " = :fieldValue" + i + " ";
            }
        } else { // Check within tenant
            for (int i = 0; i < fieldNames.length; i++) {
                queryStr += (i == 0 ? "" : " AND ")
                        + (Constants.ID.equalsIgnoreCase(fieldNames[i]) ? "primaryKey." : "")
                        + fieldNames[i] + " = :fieldValue" + i + " ";
            }
        }

        if (!Tenant.class.isAssignableFrom(validationObject.getType())) {
            queryStr += " AND t.primaryKey.tenantId = :tenantId ";
        }

        queryStr += excludeClosure;

        query = entityManager.createQuery(queryStr);

        if (id != null) {
            query.setParameter("id", id);
        }
        if (tenantId != null) {
            query.setParameter("tenantId", tenantId);
        }

        List<Field> fields = EmlogisUtils.getAllFields(validationObject.getType());
        for (int i = 0; i < fieldNames.length; i++) {
            Field field = EmlogisUtils.findFieldByName(fields, fieldNames[i]);
            if (field != null && field.getType() == DateTime.class) {
                query.setParameter("fieldValue" + i, new DateTime(values[i]));
            } else if (field != null && field.getType() == LocalDate.class) {
                query.setParameter("fieldValue" + i, new LocalDate(values[i]));
            } else {
                query.setParameter("fieldValue" + i, values[i]);
            }
        }

        return query.getResultList().size() == 0;
    }
}
