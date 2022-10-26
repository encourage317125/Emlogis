package com.emlogis.common.facade.shiftpattern;

import com.emlogis.common.Constants;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.services.shiftpattern.ShiftTypeService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.shiftpattern.ShiftType;
import com.emlogis.model.shiftpattern.dto.ShiftTypeCreateDto;
import com.emlogis.model.shiftpattern.dto.ShiftTypeDto;
import com.emlogis.model.shiftpattern.dto.ShiftTypeUpdateDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftTypeFacade extends BaseFacade {

    @EJB
    private ShiftTypeService shiftTypeService;

    @EJB
    private ShiftLengthService shiftLengthService;

    @Validation
    public ResultSetDto<ShiftTypeDto> getObjects(
            String tenantId,
            String select,        // select is NOT IMPLEMENTED FOR NOW ..
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setOffset(offset).setLimit(limit).setOrderByField(orderBy).setFilter(filter)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<ShiftType> resultSet = shiftTypeService.findShiftTypes(simpleQuery);
        return toResultSetDto(resultSet, ShiftTypeDto.class);
    }

    @Validation
    public ShiftTypeDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftType shiftType = shiftTypeService.getShiftType(primaryKey);
        return toDto(shiftType, ShiftTypeDto.class);
    }

    @Validation
    public ShiftTypeDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class) PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftTypeDto.NAME, min = 1, max = 256, passNull = true),
                            @ValidateStrLength(field = ShiftTypeDto.DESCRIPTION, min = 1, max = 256, passNull = true)
                    }
            )
            ShiftTypeUpdateDto shiftTypeUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    	
        // TODO add validation for:lengthInMin, paidTimeInMin

        boolean modified = false;
        ShiftType shiftType = shiftTypeService.getShiftType(primaryKey);

        if (StringUtils.isNotBlank(shiftTypeUpdateDto.getName())) {
            shiftType.setName(shiftTypeUpdateDto.getName());
            modified = true;
        }
        if (shiftTypeUpdateDto.getPaidTimeInMin() != null) {
        	shiftType.setPaidTimeInMin(shiftTypeUpdateDto.getPaidTimeInMin());
            modified = true;
        }
        if (StringUtils.isNotBlank(shiftTypeUpdateDto.getDescription())) {
            shiftType.setDescription(shiftTypeUpdateDto.getDescription());
            modified = true;
        }
        if (shiftTypeUpdateDto.getStartTime() != null) {
            shiftType.setStartTime(new LocalTime(shiftTypeUpdateDto.getStartTime()));
            modified = true;
        }

        if (shiftType.isActive() != shiftTypeUpdateDto.isActive()) {
        	shiftType.setActive(shiftTypeUpdateDto.isActive());
            modified = true;
        }
/*        
        if (shiftType.getLengthInMin() != shiftTypeUpdateDto.getLengthInMin()) {
        	shiftType.setLengthInMin(shiftTypeUpdateDto.getLengthInMin());
            modified = true;
        }
        if (shiftType.getPaidTimeInMin() != shiftTypeUpdateDto.getPaidTimeInMin()) {
        	shiftType.setPaidTimeInMin(shiftTypeUpdateDto.getPaidTimeInMin());
            modified = true;
        }
*/        
        if (modified) {
            setUpdatedBy(shiftType);
            shiftType = shiftTypeService.update(shiftType);
        }

        return toDto(shiftType, ShiftTypeDto.class);
    }

    @Validation
    public ShiftTypeDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class, expectedResult = false)
            PrimaryKey primaryKey,
            @ValidateStrLength(field = "shiftLengthId", min = 1, passNull = false)
            ShiftTypeCreateDto shiftTypeCreateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ShiftType shiftType = shiftTypeService.create(primaryKey);

        PrimaryKey lengthPrimaryKey = new PrimaryKey(primaryKey.getTenantId(), shiftTypeCreateDto.getShiftLengthId());
        ShiftLength shiftLength = shiftLengthService.getShiftLength(lengthPrimaryKey);
        shiftType.setShiftLength(shiftLength);

        ShiftTypeUpdateDto updateDto = shiftTypeCreateDto.getUpdateDto();
        if (updateDto != null) {
            shiftType.setStartTime(new LocalTime(updateDto.getStartTime()));
            shiftType.setName(updateDto.getName());
            shiftType.setPaidTimeInMin(updateDto.getPaidTimeInMin() == null ? 0 : updateDto.getPaidTimeInMin());
        }

        setCreatedBy(shiftType);
        setOwnedBy(shiftType);

        shiftType = shiftTypeService.update(shiftType);

        return toDto(shiftType, ShiftTypeDto.class);
    }

    @Validation
    public void deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftType.class) PrimaryKey primaryKey) {
        ShiftType shiftType = shiftTypeService.getShiftType(primaryKey);
        shiftTypeService.delete(shiftType);
    }
}
