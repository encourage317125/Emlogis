package com.emlogis.common.facade.shiftpattern;

import com.emlogis.common.Constants;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.validation.annotations.*;
import com.emlogis.common.validation.validators.EntityExistValidatorBean;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.shiftpattern.dto.ShiftLengthCreateDto;
import com.emlogis.model.shiftpattern.dto.ShiftLengthDto;
import com.emlogis.model.shiftpattern.dto.ShiftLengthUpdateDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftLengthFacade extends BaseFacade {

    @EJB
    private ShiftLengthService shiftLengthService;

    @Validation
    public ResultSetDto<ShiftLengthDto> getObjects(
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
        ResultSet<ShiftLength> resultSet = shiftLengthService.findShiftLengths(simpleQuery);
        return toResultSetDto(resultSet, ShiftLengthDto.class);
    }

    @Validation
    public ShiftLengthDto getObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class)
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShiftLength shiftLength = shiftLengthService.getShiftLength(primaryKey);
        return toDto(shiftLength, ShiftLengthDto.class);
    }

    @Validation
    public ShiftLengthDto updateObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class) PrimaryKey primaryKey,
            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftLengthDto.NAME, min = 1, max = 256, passNull = true)
                    }
            )
            ShiftLengthUpdateDto shiftLengthUpdateDto)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        boolean modified = false;
        ShiftLength shiftLength = shiftLengthService.getShiftLength(primaryKey);

        if (StringUtils.isNotBlank(shiftLengthUpdateDto.getName())) {
            shiftLength.setName(shiftLengthUpdateDto.getName());
            modified = true;
        }
        if (StringUtils.isNotBlank(shiftLengthUpdateDto.getDescription())) {
            shiftLength.setDescription(shiftLengthUpdateDto.getDescription());
            modified = true;
        }
        if (shiftLengthUpdateDto.getLengthInMin() != null) {
            shiftLength.setLengthInMin(shiftLengthUpdateDto.getLengthInMin());
            modified = true;
        }

        if (modified) {
            setUpdatedBy(shiftLength);
            shiftLength = shiftLengthService.update(shiftLength);
        }

        return toDto(shiftLength, ShiftLengthDto.class);
    }

    @Validation
    public ShiftLengthDto createObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class, expectedResult = false)
            PrimaryKey primaryKey,

            @ValidateAll(
                    strLengths = {
                            @ValidateStrLength(field = ShiftLengthDto.NAME, min = 1, max = 256)
                    }
            )
            ShiftLengthCreateDto shiftLengthCreateDto) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ShiftLength shiftLength = shiftLengthService.create(primaryKey);
        shiftLength.setName(shiftLengthCreateDto.getName());

        ShiftLengthUpdateDto updateDto = shiftLengthCreateDto.getUpdateDto();
        if (updateDto != null) {
            shiftLength.setDescription(updateDto.getDescription());
            shiftLength.setLengthInMin(updateDto.getLengthInMin() == null ? 0 : updateDto.getLengthInMin());
        }

        setCreatedBy(shiftLength);
        setOwnedBy(shiftLength);

        shiftLength = shiftLengthService.update(shiftLength);

        return toDto(shiftLength, ShiftLengthDto.class);
    }

    @Validation
    public void deleteObject(
            @Validate(validator = EntityExistValidatorBean.class, type = ShiftLength.class) PrimaryKey primaryKey) {
        ShiftLength shiftLength = shiftLengthService.getShiftLength(primaryKey);
        shiftLengthService.delete(shiftLength);
    }

    public ResultSetDto<ShiftLengthDto> query(String tenantId,
                                              boolean hasShiftType,
                                              String siteId,
                                              Integer shiftLengthInMin,
                                              int offset,
                                              int limit,
                                              String orderBy,
                                              String orderDir) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        ResultSet<ShiftLength> lengthResultSet = shiftLengthService.query(tenantId, hasShiftType, siteId,
                shiftLengthInMin, offset, limit, orderBy, orderDir);
        return toResultSetDto(lengthResultSet, ShiftLengthDto.class);
    }
}
