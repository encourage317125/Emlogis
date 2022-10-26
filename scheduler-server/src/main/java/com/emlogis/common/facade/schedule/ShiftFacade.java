package com.emlogis.common.facade.schedule;

import com.emlogis.common.Constants;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.schedule.dto.ShiftDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftFacade extends BaseFacade {

    @EJB
    private ShiftService shiftService;

    @Validation
    public ResultSetDto<ShiftDto> getObjects(
            String tenantId,
            String select,
            String filter,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        SimpleQuery simpleQuery = new SimpleQuery(tenantId);
        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equals(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Shift> resultSet = shiftService.findShifts(simpleQuery);
        return toResultSetDto(resultSet, ShiftDto.class);
    }

    @Validation
    public ResultSetDto<Object[]> getObjects(
            String employeeId,
            long startDate,
            long endDate,
            String timeZone,
            String scheduleStatus,
            String returnedFields,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ResultSet<Object[]> resultSet= shiftService.getShifts(employeeId, startDate, endDate, timeZone,
                ScheduleStatus.valueOf(scheduleStatus).ordinal(), returnedFields, offset, limit, orderBy, orderDir, false);
        ResultSetDto<Object[]> resultSetDto = new ResultSetDto<>();
        resultSetDto.setResult(resultSet.getResult());
        resultSetDto.setTotal(resultSet.getTotal());
        return resultSetDto;
    }

    @Validation
    public ResultSetDto<Object[]> getScheduleAndProdPostedShifts(
            String employeeId,
            String scheduleId,
            long startDate,
            long endDate,
            String returnedFields,
            int offset,
            int limit,
            @ValidatePaging(name = Constants.ORDER_BY)
            String orderBy,
            @ValidatePaging(name = Constants.ORDER_DIR)
            String orderDir) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        ResultSet<Object[]> resultSet= shiftService.getScheduleAndProdPostedShifts(employeeId, scheduleId, startDate,
                endDate, returnedFields, offset, limit, orderBy, orderDir, false);
        ResultSetDto<Object[]> resultSetDto = new ResultSetDto<>();
        resultSetDto.setResult(resultSet.getResult());
        resultSetDto.setTotal(resultSet.getTotal());
        return resultSetDto;
    }

}
