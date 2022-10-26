package com.emlogis.common.facade.contract;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ejb.*;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;

import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.contract.ContractLineService;
import com.emlogis.common.services.contract.ContractService;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.BooleanCL;
import com.emlogis.model.contract.Contract;
import com.emlogis.model.contract.ContractLine;
import com.emlogis.model.contract.ContractConfig;
import com.emlogis.model.contract.IntMinMaxCL;
import com.emlogis.model.contract.WeekdayRotationPatternCL;
import com.emlogis.model.contract.WeekendWorkPatternCL;
import com.emlogis.model.contract.dto.BooleanCLDTO;
import com.emlogis.model.contract.dto.ContractLineCreateDto;
import com.emlogis.model.contract.dto.ContractLineDTO;
import com.emlogis.model.contract.dto.ContractLineUpdateDto;
import com.emlogis.model.contract.dto.IntMinMaxCLDTO;
import com.emlogis.model.contract.dto.OvertimeDto;
import com.emlogis.model.contract.dto.WeekdayRotationPatternCLDTO;
import com.emlogis.model.contract.dto.WeekendWorkPatternCLDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

/**
 * @author emlogis
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ContractLineFacade extends BaseFacade {

    @EJB
    ContractLineService contractLineService;

    @EJB
    ContractService contractService;

    /**
     * Getter for the contractLineService field
     *
     * @return
     */
    protected ContractLineService getContractService() {
        return contractLineService;
    }

    /**
     * Get queried collection of Contract Lines for a contract
     *
     * @param tenantId
     * @param contractId
     * @param select
     * @param filter
     * @param offset
     * @param limit
     * @param orderBy
     * @param orderDir
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    @Validation
    public ResultSetDto<ContractLineDTO> getObjects(
            String tenantId,
            String contractId,
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

        filter = "contract.primaryKey.id=" + "'" + contractId + "' ";

        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<ContractLine> rs = contractLineService.findContractLines(simpleQuery);

        Map<Class<? extends ContractLine>, Class<? extends ContractLineDTO>> classMap = getContractLineClassMap();

        return toResultSetDto(rs, classMap);
    }

    public ContractLineDTO getObject(
            PrimaryKey primaryKey)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        ContractLine contractLine = contractLineService.getContractLine(primaryKey);

        if (contractLine == null) {
            throw new ValidationException("A contract line cannot be found with the specified id.");
        }

        Map<Class<? extends ContractLine>, Class<? extends ContractLineDTO>> classMap = getContractLineClassMap();

        return toDto(contractLine, classMap);
    }


    public ContractLineDTO createObject(PrimaryKey primaryKey,
                                        ContractLineCreateDto contractLineCreateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ContractLine contractLine;

        PrimaryKey contractKey = new PrimaryKey(primaryKey.getTenantId(), contractLineCreateDto.getContractId());

        Contract contract = contractService.getContract(contractKey);
        if (contract == null) {
            throw new ValidationException("Could not locate a contract with the id supplied.");
        }

        ContractLineType contractLineType = contractLineCreateDto.getContractLineType();
        if (contractLineType == null) {
            throw new ValidationException("The contract line type is required.");
        }

        // Verify this contractline type is not more than the maximum for this type allowed
        Integer maxForType = ContractConfig.getMaxForCLType(contract, contractLineType);
        if (maxForType == null) {
            throw new ValidationException("The contract line type is allowed for this contract.");
        }

        int currentCLTypeCount = contract.getContractLineTypeCount(contractLineType);
        if (++currentCLTypeCount > maxForType) {
            throw new ValidationException("This contract already has the maximum contract lines of type: "
                    + contractLineType);
        }

        switch (contractLineCreateDto.getContractLineType()) {
            case CONSECUTIVE_WORKING_DAYS :
            case HOURS_BETWEEN_DAYS :
            case HOURS_PER_DAY :
            case HOURS_PER_WEEK :
            case DAYS_PER_WEEK :
            case HOURS_PER_WEEK_PRIME_SKILL :
            case DAILY_OVERTIME :
            case WEEKLY_OVERTIME :
            case TWO_WEEK_OVERTIME :
                contractLine = new IntMinMaxCL(primaryKey, contractLineCreateDto.getName(),
                        contractLineCreateDto.getCategory(), contractLineCreateDto.getContractLineType(),
                        contract, contractLineCreateDto.isMinimumEnabled(), contractLineCreateDto.getMinimumValue(),
                        contractLineCreateDto.getMinimumWeight(), contractLineCreateDto.isMaximumEnabled(),
                        contractLineCreateDto.getMaximumValue(),
                        contractLineCreateDto.getMaximumWeight());
                break;
            case COMPLETE_WEEKENDS:
                contractLine = new WeekendWorkPatternCL(primaryKey, contractLineCreateDto.getName(),
                        contractLineCreateDto.getCategory(), contractLineCreateDto.getContractLineType(),
                        contract, contractLineCreateDto.getWeight(), contractLineCreateDto.getDaysOffAfter(),
                        contractLineCreateDto.getDaysOffBefore());
                break;
            case CUSTOM:
                contractLine = new WeekdayRotationPatternCL(primaryKey, contractLineCreateDto.getName(),
                        contractLineCreateDto.getCategory(), contractLineCreateDto.getContractLineType(),
                        contract, contractLineCreateDto.getWeight(), contractLineCreateDto.getDayOfWeek(),
                        contractLineCreateDto.getNumberOfDays(), contractLineCreateDto.getOutOfTotalDays(),
                        contractLineCreateDto.getRotationType());
                break;
            default:
                throw new ValidationException("Invalid contract line type.");
        }

        contractLineService.createContractLine(contractLine);

        contract.getContractLines().add(contractLine);
        contract.updateContractLineTypeCount(contractLineType, currentCLTypeCount);

        contractService.updateContract(contract);

        setCreatedBy(contractLine);
        setUpdatedBy(contractLine);

        contractLine = contractLineService.updateContractLine(contractLine);

        Map<Class<? extends ContractLine>, Class<? extends ContractLineDTO>> classMap = getContractLineClassMap();

        return toDto(contractLine, classMap);
    }

    // For update, I'm assuming we don't want to update contractline type or contractid

    public ContractLineDTO updateObject(PrimaryKey primaryKey,
                                        ContractLineUpdateDto contractLineUpdateDto) throws InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ContractLine contractLine = contractLineService.getContractLine(primaryKey);

        if (contractLine == null) {
            throw new ValidationException("Could not locate a contract line with the id supplied.");
        }

        if (!StringUtils.isBlank(contractLineUpdateDto.getCategory())) {
            contractLine.setCategory(contractLineUpdateDto.getCategory());
        }

        if (!StringUtils.isBlank(contractLineUpdateDto.getName())) {
            contractLine.setName(contractLineUpdateDto.getName());
        }

        ContractLineType contractLineType = contractLine.getContractLineType();

        switch (contractLineType) {
            case CONSECUTIVE_WORKING_DAYS:
            case HOURS_BETWEEN_DAYS:
            case HOURS_PER_DAY:
            case HOURS_PER_WEEK:
            case DAYS_PER_WEEK:
            case HOURS_PER_WEEK_PRIME_SKILL:
    		case DAILY_OVERTIME :
    		case WEEKLY_OVERTIME :
    		case TWO_WEEK_OVERTIME :
                IntMinMaxCL intMinMaxCL = (IntMinMaxCL) contractLine;
                intMinMaxCL.setMinimumEnabled(contractLineUpdateDto.isMinimumEnabled());
                intMinMaxCL.setMinimumValue(contractLineUpdateDto.getMinimumValue());
                intMinMaxCL.setMinimumWeight(contractLineUpdateDto.getMinimumWeight());

                intMinMaxCL.setMaximumEnabled(contractLineUpdateDto.isMaximumEnabled());
                intMinMaxCL.setMaximumValue(contractLineUpdateDto.getMaximumValue());
                intMinMaxCL.setMaximumWeight(contractLineUpdateDto.getMaximumWeight());

                break;

            case CUSTOM:
                WeekdayRotationPatternCL weekdayRotationPatternCL = (WeekdayRotationPatternCL) contractLine;
                weekdayRotationPatternCL.setWeight(contractLineUpdateDto.getWeight());
                weekdayRotationPatternCL.setDayOfWeek(contractLineUpdateDto.getDayOfWeek());
                weekdayRotationPatternCL.setNumberOfDays(contractLineUpdateDto.getNumberOfDays());
                weekdayRotationPatternCL.setOutOfTotalDays(contractLineUpdateDto.getOutOfTotalDays());
                weekdayRotationPatternCL.setRotationType(contractLineUpdateDto.getRotationType());
                break;

            case COMPLETE_WEEKENDS:
                WeekendWorkPatternCL weekendWorkPatternCL = (WeekendWorkPatternCL) contractLine;
                weekendWorkPatternCL.setWeight(contractLineUpdateDto.getWeight());
                weekendWorkPatternCL.setDaysOffAfter(contractLineUpdateDto.getDaysOffAfter());
                weekendWorkPatternCL.setDaysOffBefore(contractLineUpdateDto.getDaysOffBefore());
                break;

            default:
                break;
        }


        contractLine = contractLineService.updateContractLine(contractLine);

        Map<Class<? extends ContractLine>, Class<? extends ContractLineDTO>> classMap = getContractLineClassMap();

        return toDto(contractLine, classMap);
    }

    public boolean deleteObject(PrimaryKey primaryKey, String contractId) {
        ContractLine contractLine = contractLineService.getContractLine(primaryKey);

        if (contractLine == null) {
            throw new ValidationException("Could not locate a contract line with the id supplied.");
        }

        PrimaryKey contractKey = new PrimaryKey(primaryKey.getTenantId(), contractId);

        Contract contract = contractService.getContract(contractKey);

        if (contract == null) {
            throw new ValidationException("Could not locate a contract with the id supplied.");
        }

        ContractLineType contractLineType = contractLine.getContractLineType();

        if (contractLineType == null) {
            throw new ValidationException("The contract line type is required.");
        }

        // Decrement the count for this contract line type
        int currentCLTypeCount = contract.getContractLineTypeCount(contractLineType);
        contract.getContractLines().remove(contractLine);
        contract.updateContractLineTypeCount(contractLineType, --currentCLTypeCount);
        contract = contractService.updateContract(contract);

        contractLineService.delete(contractLine);

        return true;
    }
    
    public void updateOverTimeContractLines(Contract contract, OvertimeDto overtimeDto) throws InstantiationException,
		IllegalAccessException, NoSuchMethodException, InvocationTargetException{
		// Get DTO values
		int dailyOvertimeMins = overtimeDto.getDailyOvertimeMins();
		int weeklyOvertimeMins = overtimeDto.getWeeklyOvertimeMins();
		int biweeklyOvertimeMins = overtimeDto.getBiweeklyOvertimeMins();
		
		Set<ContractLine> contractLines = contract.getContractLines();
		
		IntMinMaxCL dailyOverTimeCL = null;
		IntMinMaxCL weeklyOverTimeCL = null;
		IntMinMaxCL biWeeklyOverTimeCL = null;
		
		ContractLineType contractLineType;
		
		for (ContractLine contractLine: contractLines) {
			contractLineType = contractLine.getContractLineType();
			switch (contractLineType) {
                case DAILY_OVERTIME :
                    dailyOverTimeCL = (IntMinMaxCL) contractLine;
                    break;

                case WEEKLY_OVERTIME :
                    weeklyOverTimeCL = (IntMinMaxCL) contractLine;
                    break;

                case TWO_WEEK_OVERTIME :
                    biWeeklyOverTimeCL = (IntMinMaxCL) contractLine;
                    break;

				default:
					break;
			}
			
		}		
		
		updateOvertimeContractLine(contract, dailyOvertimeMins, dailyOverTimeCL, ContractLineType.DAILY_OVERTIME);
		updateOvertimeContractLine(contract, weeklyOvertimeMins, weeklyOverTimeCL, ContractLineType.WEEKLY_OVERTIME);
		updateOvertimeContractLine(contract, biweeklyOvertimeMins, biWeeklyOverTimeCL, ContractLineType.TWO_WEEK_OVERTIME);
    }

	private void updateOvertimeContractLine(Contract contract,
			int overTimeCLValue, IntMinMaxCL overTimeCL, ContractLineType contractLineType)
			throws InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		if (overTimeCLValue == -1) {
			if (overTimeCL != null) {
				// remove any existing contract line
				deleteObject(overTimeCL.getPrimaryKey(), contract.getId());
			}
		} else if(overTimeCL == null) {
			// Create contract Line and set Value
			
			ContractLineCreateDto contractLineCreateDto = new ContractLineCreateDto();
			contractLineCreateDto.setContractLineType(contractLineType);
			contractLineCreateDto.setName(contractLineType.toString());
			contractLineCreateDto.setMaximumEnabled(true);
			contractLineCreateDto.setMaximumValue(overTimeCLValue);
			contractLineCreateDto.setMaximumWeight(-1);
			contractLineCreateDto.setContractId(contract.getId());
			contractLineCreateDto.setMinimumEnabled(false);
			
			PrimaryKey primaryKey = new PrimaryKey(contract.getTenantId());
			
			createObject(primaryKey, contractLineCreateDto);
		} else {
			// just need to set the value
			overTimeCL.setMaximumEnabled(true);
			overTimeCL.setMaximumValue(overTimeCLValue);
			overTimeCL.setMaximumWeight(-1);			
		}
	} 

	public OvertimeDto getOverTimeCLValues(Contract contract) {
		OvertimeDto overtimeDto = new OvertimeDto();
		
		overtimeDto.setDailyOvertimeMins(-1);
		overtimeDto.setBiweeklyOvertimeMins(-1);
		overtimeDto.setWeeklyOvertimeMins(-1);	
		
		Set<ContractLine> contractLines = contract.getContractLines();
		
		IntMinMaxCL dailyOverTimeCL = null;
		IntMinMaxCL weeklyOverTimeCL = null;
		IntMinMaxCL biWeeklyOverTimeCL = null;
		
		ContractLineType contractLineType;
		
		for (ContractLine contractLine: contractLines) {
			contractLineType = contractLine.getContractLineType();
			
			switch (contractLineType) {
                case DAILY_OVERTIME :
                    dailyOverTimeCL = (IntMinMaxCL) contractLine;
                    break;

                case WEEKLY_OVERTIME :
                    weeklyOverTimeCL = (IntMinMaxCL) contractLine;
                    break;

                case TWO_WEEK_OVERTIME :
                    biWeeklyOverTimeCL = (IntMinMaxCL) contractLine;
                    break;
				
				default:
					break;
			}
			
		}	
		
		if (dailyOverTimeCL != null && dailyOverTimeCL.getMaximumEnabled())
			overtimeDto.setDailyOvertimeMins( dailyOverTimeCL.getMaximumValue() );
		
		if (weeklyOverTimeCL != null && weeklyOverTimeCL.getMaximumEnabled())
			overtimeDto.setWeeklyOvertimeMins( weeklyOverTimeCL.getMaximumValue() );
		
		if (biWeeklyOverTimeCL != null && biWeeklyOverTimeCL.getMaximumEnabled())
			overtimeDto.setBiweeklyOvertimeMins( biWeeklyOverTimeCL.getMaximumValue() );

		return overtimeDto;
	}

    private Map<Class<? extends ContractLine>, Class<? extends ContractLineDTO>> getContractLineClassMap() {
        Map<Class<? extends ContractLine>, Class<? extends ContractLineDTO>> classMap = new HashMap<>();
        classMap.put(BooleanCL.class, BooleanCLDTO.class);
        classMap.put(IntMinMaxCL.class, IntMinMaxCLDTO.class);
        classMap.put(WeekendWorkPatternCL.class, WeekendWorkPatternCLDTO.class);
        classMap.put(WeekdayRotationPatternCL.class, WeekdayRotationPatternCLDTO.class);
        return classMap;
    }

}
