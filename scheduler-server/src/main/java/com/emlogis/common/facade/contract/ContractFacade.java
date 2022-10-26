package com.emlogis.common.facade.contract;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.*;

import org.apache.commons.lang3.StringUtils;

import com.emlogis.common.Constants;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.BaseFacade;
import com.emlogis.common.services.contract.ContractService;
import com.emlogis.common.validation.annotations.ValidatePaging;
import com.emlogis.common.validation.annotations.Validation;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.contract.Contract;
import com.emlogis.model.contract.EmployeeContract;
import com.emlogis.model.contract.SiteContract;
import com.emlogis.model.contract.TeamContract;
import com.emlogis.model.contract.dto.ContractDTO;
import com.emlogis.model.contract.dto.EmployeeContractDTO;
import com.emlogis.model.contract.dto.OvertimeDto;
import com.emlogis.model.contract.dto.SiteContractDTO;
import com.emlogis.model.contract.dto.TeamContractDTO;
import com.emlogis.model.dto.ResultSetDto;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ContractFacade extends BaseFacade {

    @EJB
    private ContractService contractService;
    
    @EJB
    private ContractLineFacade contractLineFacade;

    /**
     * Getter for the contractService field
     *
     * @return
     */
    protected ContractService getContractService() {
        return contractService;
    }


    /**
     * Get queried collection of Contracts
     *
     * @param tenantId
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
    public ResultSetDto<ContractDTO> getObjects(
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

        simpleQuery.setSelect(select).setFilter(filter).setOffset(offset).setLimit(limit).setOrderByField(orderBy)
                .setOrderAscending(StringUtils.equalsIgnoreCase(orderDir, "ASC")).setTotalCount(true);
        ResultSet<Contract> rs = contractService.findContracts(simpleQuery);

        Map<Class<? extends Contract>, Class<? extends ContractDTO>> classMap = getContractClassMap();

        return toResultSetDto(rs, classMap);
    }

    /**
     * Get specified Contract
     *
     * @param primaryKey
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */

    public ContractDTO getObject(PrimaryKey primaryKey) throws InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        Contract contract = contractService.getContract(primaryKey);
        if (contract == null) {
            throw new ValidationException("A contract cannot be found with the specified id.");
        }

        Map<Class<? extends Contract>, Class<? extends ContractDTO>> classMap = getContractClassMap();

        return toDto(contract, classMap);
    }

    /**
     * Create Employee Contract
     *
     * @param employee
     * @param isDefaultContract
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */

    public EmployeeContract createEmployeeContract(Employee employee, boolean isDefaultContract) {
        PrimaryKey contractKey = new PrimaryKey(employee.getPrimaryKey().getTenantId());
        EmployeeContract employeeContract = contractService.createEmployeeContract(contractKey);
        employeeContract.setEmployee(employee);
        employeeContract.setDefaultContract(isDefaultContract);
        setCreatedBy(employeeContract);
        setOwnedBy(employeeContract, null);
        employeeContract = (EmployeeContract) contractService.updateContract(employeeContract);

        return employeeContract;
    }

    public SiteContract createSiteContract(Site site, boolean isDefaultContract) {
        PrimaryKey contractKey = new PrimaryKey(site.getPrimaryKey().getTenantId());
        SiteContract siteContract = contractService.createSiteContract(contractKey);
        siteContract.setSite(site);
        siteContract.setDefaultContract(isDefaultContract);
        setCreatedBy(siteContract);
        setOwnedBy(siteContract, null);
        siteContract = (SiteContract) contractService.updateContract(siteContract);

        return siteContract;
    }

    public TeamContract createTeamContract(Team team, boolean isDefaultContract) {
        PrimaryKey contractKey = new PrimaryKey(team.getPrimaryKey().getTenantId());
        TeamContract teamContract = contractService.createTeamContract(contractKey);
        teamContract.setTeam(team);
        teamContract.setDefaultContract(isDefaultContract);
        setCreatedBy(teamContract);
        setOwnedBy(teamContract, null);
        teamContract = (TeamContract) contractService.updateContract(teamContract);

        return teamContract;
    }
    
    public void updateOverTimeContractLines(Contract contract, OvertimeDto overtimeDto) throws InstantiationException,
    		IllegalAccessException, NoSuchMethodException, InvocationTargetException{
    	contractLineFacade.updateOverTimeContractLines(contract, overtimeDto);
    }
    
    public OvertimeDto getOverTimeCLValues(Contract contract) {
    	return contractLineFacade.getOverTimeCLValues(contract);
    	
    }

    private Map<Class<? extends Contract>, Class<? extends ContractDTO>> getContractClassMap() {
        Map<Class<? extends Contract>, Class<? extends ContractDTO>> classMap = new HashMap<>();
        classMap.put(EmployeeContract.class, EmployeeContractDTO.class);
        classMap.put(TeamContract.class, TeamContractDTO.class);
        classMap.put(SiteContract.class, SiteContractDTO.class);
        return classMap;
    }

}
