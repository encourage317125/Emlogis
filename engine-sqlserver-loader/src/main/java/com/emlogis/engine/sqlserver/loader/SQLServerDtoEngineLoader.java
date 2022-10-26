package com.emlogis.engine.sqlserver.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.ShiftDate;
import com.emlogis.engine.domain.ShiftSkillRequirement;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.emlogis.engine.domain.contract.contractline.ContractScope;
import com.emlogis.engine.domain.contract.contractline.dto.IntMinMaxCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.WeekdayRotationPatternCLDto;
import com.emlogis.engine.domain.contract.contractline.dto.WeekendWorkPatternCLDto;
import com.emlogis.engine.domain.contract.dto.ConstraintOverrideDto;
import com.emlogis.engine.domain.contract.dto.ContractDto;
import com.emlogis.engine.domain.contract.patterns.WeekdayRotationPattern.RotationPatternType;
import com.emlogis.engine.domain.dto.AssignmentRequestDto;
import com.emlogis.engine.domain.dto.EmployeeDto;
import com.emlogis.engine.domain.dto.EmployeeRosterDto;
import com.emlogis.engine.domain.dto.EmployeeSkillDto;
import com.emlogis.engine.domain.dto.EmployeeTeamDto;
import com.emlogis.engine.domain.dto.ShiftDto;
import com.emlogis.engine.domain.dto.SkillDto;
import com.emlogis.engine.domain.organization.TeamAssociationType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.timeoff.PreferenceType;
import com.emlogis.engine.domain.timeoff.dto.CDPreferenceDto;
import com.emlogis.engine.domain.timeoff.dto.CDTimeOffDto;
import com.emlogis.engine.domain.timeoff.dto.CIPreferenceDto;
import com.emlogis.engine.domain.timeoff.dto.CITimeOffDto;
import com.emlogis.engine.domain.timeoff.dto.TimeWindowDto;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeContractLine;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeIDToTeamID;
import com.emlogis.engine.sqlserver.loader.domain.EmployeeSkill;
import com.emlogis.engine.sqlserver.loader.domain.ProductionShiftSummary;
import com.emlogis.engine.sqlserver.loader.domain.ShiftDemand;
import com.emlogis.engine.sqlserver.loader.domain.SiteContractLine;
import com.emlogis.engine.sqlserver.loader.domain.T_Employee;
import com.emlogis.engine.sqlserver.loader.domain.T_EmployeeCIRotation;
import com.emlogis.engine.sqlserver.loader.domain.T_EmployeeWeekend;
import com.emlogis.engine.sqlserver.loader.domain.T_Site;
import com.emlogis.engine.sqlserver.loader.domain.T_SiteResource;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCDAvailability;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCDPreference;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCIAvailability;
import com.emlogis.engine.sqlserver.loader.domain.timeoff.T_EmployeeCIPreference;
import com.emlogis.engine.sqlserver.loader.exception.IncorrectDataLoadingOrderException;
import com.emlogis.engine.sqlserver.loader.exception.NullEntityManagerException;

public class SQLServerDtoEngineLoader {
	protected long scheduleId;
	protected int siteId;
	protected List<Long> teamIds;
	protected LocalDate planningStartDate;
	protected LocalDate scheduleEndDate;
	// Planning Engine objects
	private Map<Long, SkillDto> skillsMap;
	protected EmployeeRosterDto employeeRoster;
	private List<ShiftSkillRequirement> shiftSkillRequirementsList;
	private Set<ShiftDate> shiftDateList;
	private Map<Long, EmployeeDto> employeeMap;
	private Map<Long, ContractDto> employeeContracts;

	private AssignmentRequestDto startingSolution;
	private SQLServerDatabaseLoader databaseLoader;

	public static final int DISABLED_MAX_VALUE = 999;
	public static final int DISABLED_MIN_VALUE = 0;
	 
	// RTO Bit Mask values
    public static final long IGNORE_MIN_HOURS_BETWEEN_DAYS = 1;
    public static final long ENABLE_FLOAT = 2;
    public static final long AVOID_SCHEDULING = 8;
    public static final long AVOID_OVERTIME = 16;
    public static final long IGNORE_MIN_HOURS_PER_WEEK = 32;
    public static final long IGNORE_PAID_TIME_OFF = 64;
    public static final long IGNORE_UNAVAILABLE = 256;
    public static final long IGNORE_DAILY_TIME_LIMIT = 512;
    public static final long IGNORE_MAX_DAYS_PER_WEEK = 1024;
    public static final long IGNORE_DAY_ROTATION = 2048;
    public static final long IGNORE_MAX_HOURS_PER_WEEK = 4096;
    public static final long IGNORE_MAX_HOURS_PER_DAY = 16384;
    public static final long IGNORE_MIN_HOURS_PER_DAY = 32768;
    public static final long IGNORE_COUPLED_WEEKENDS = 524288;
    public static final long IGNORE_DAYS_BEFORE = 1048576;
    public static final long IGNORE_DAYS_AFTER = 2097152;
    public static final long IGNORE_MAX_CONSECUTIVE_DAYS = 8192;
    
	public SQLServerDtoEngineLoader(long scheduleId, int siteId, List<Long> teamIds, LocalDate scheduleStartDate, LocalDate scheduleEndDate) {
		this.scheduleId = scheduleId;
		this.siteId = siteId;
		this.teamIds = teamIds;
		this.planningStartDate = scheduleStartDate;
		this.scheduleEndDate = scheduleEndDate;
		
		employeeRoster = new EmployeeRosterDto();
		
		databaseLoader = new SQLServerDatabaseLoader();

		// Initialize containers
		shiftSkillRequirementsList = new ArrayList<ShiftSkillRequirement>();
		shiftDateList = new HashSet<ShiftDate>();
		skillsMap = new HashMap<Long, SkillDto>();
		employeeMap = new HashMap<Long, EmployeeDto>();
		employeeContracts = new HashMap<Long, ContractDto>();

		startingSolution = new AssignmentRequestDto();
	}

	/**
	 * Retrieves site information from Database and sets the 1st day of week as
	 * well as the schedule planning window and the first day for which to load
	 * ShiftDto assignment data(before the start of planning window)
	 * 
	 */
	private void loadSiteInformation() {
		T_Site site = null;
		try {
			site = databaseLoader.loadSiteInformation(siteId);
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
			return;
		}

		//Load overtime start(used by two week overtime constraint)
		employeeRoster.setTwoWeekOvertimeStartDate(new ShiftDate(site.getOvertimeStartDate()));
		
		//In Aspen weekend definition is always Saturday.Sunday 
		employeeRoster.setWeekendDefinition(WeekendDefinition.SATURDAY_SUNDAY);
		
		DayOfWeek firstDayOfWeek = valueOfCalendar(site.getFirstDayOfWeek());
		employeeRoster.setFirstDayOfWeek(firstDayOfWeek);

		ShiftDate schedulePlanningStartShiftDate = new ShiftDate(planningStartDate);
		employeeRoster.setPlanningWindowStart(schedulePlanningStartShiftDate);

		// Make start of schedule the 1st day of the week before the first
		// planning day
		ShiftDate startOfSchedulingShiftDate = schedulePlanningStartShiftDate.getDateOfFirstDayOfWeek(firstDayOfWeek);
		employeeRoster.setFirstShiftDate(startOfSchedulingShiftDate);

		ShiftDate scheduleEndShiftDate = new ShiftDate(scheduleEndDate);
		employeeRoster.setLastShiftDate(scheduleEndShiftDate);

		shiftDateList.add(schedulePlanningStartShiftDate);
		shiftDateList.add(scheduleEndShiftDate);
		shiftDateList.add(startOfSchedulingShiftDate);

		propagateWeekendShiftDates(startOfSchedulingShiftDate, scheduleEndShiftDate);
		
		// Prepare roster info
		setUpRuleWeights(employeeRoster);
		setUpScoringRuleScoreLevels(employeeRoster);
		startingSolution.setEmployeeRosterDto(employeeRoster);
	}

	/**
	 * Set the weight of each rule to 1 by default
	 * 
	 */
	protected void setUpRuleWeights(EmployeeRosterDto rosterInfo) {
		rosterInfo.setRuleWeightMultipliers(new HashMap<RuleName, Integer>());
		rosterInfo.putRuleWeightMultiplier(RuleName.CD_TIME_OFF_CONSTRAINT , 5);
		rosterInfo.putRuleWeightMultiplier(RuleName.CI_TIME_OFF_CONSTRAINT, 5);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_CONSECUTIVE_DAYS_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_CONSECUTIVE_12H_DAYS_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_DAYS_PER_WEEK_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_HOURS_PER_DAY_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MAX_HOURS_PER_WEEK_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_BETWEEN_DAYS_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_DAY_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_WEEK_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.MIN_HOURS_PER_TWO_WEEKS_CONSTRAINT, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.REQUIRED_EMPLOYEES_MATCH_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.SKILL_MATCH_RULE, 40);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT, 10);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT_FLOAT, 10);
		rosterInfo.putRuleWeightMultiplier(RuleName.WEEKDAY_ROTATION_PATTERN_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.COUPLED_WEEKEND_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.DAYS_OFF_AFTER_WEEKEND_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.DAYS_OFF_BEFORE_WEEKEND_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.AVOID_DAILY_OVERTIME_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.AVOID_WEEKLY_OVERTIME_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.AVOID_TWO_WEEK_OVERTIME_RULE, 1);
		rosterInfo.putRuleWeightMultiplier(RuleName.OVERLAPPING_SHIFTS_RULE, 10);	
	}
	
	/**
	 * Set default values for the soft scoring rules
	 * in future this will be loaded from the schedule
	 * generation request
	 * 
	 * @param rosterInfo
	 */
	protected void setUpScoringRuleScoreLevels(EmployeeRosterDto rosterInfo){
		rosterInfo.setScoringRulesToScoreLevelMap(new HashMap<RuleName, Integer>());
		rosterInfo.putScoringRuleScoreLevel(RuleName.CD_PREFERENCE_RULE, 2);
		rosterInfo.putScoringRuleScoreLevel(RuleName.CI_PREFERENCE_RULE, 2);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SCHEDULE_COST_RULE, 0);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SCHEDULE_OVERTIME_RULE, 1);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SENIORITY_RULE, 3);
		rosterInfo.putScoringRuleScoreLevel(RuleName.EXTRA_SHIFT_RULE, 3);
	}
	
	public void setEntityManager(EntityManager entityManager) {
		databaseLoader.setEntityManager(entityManager);
	}

	public void setKeepEntityManager(boolean keepEntityManager) {
		databaseLoader.setKeepEntityManager(keepEntityManager);
	}
	
	public List<EmployeeDto> convertEmployeesFromDB(Collection<T_Employee> employeeList) {
		List<EmployeeDto> convertedEmployees = new ArrayList<EmployeeDto>();
		for (T_Employee EmployeeDto : employeeList) {
			EmployeeDto engineEmployee = new EmployeeDto();
			engineEmployee.setId(String.valueOf(EmployeeDto.getEmployeeID()));
			engineEmployee.setLastName(EmployeeDto.getLastName());
			engineEmployee.setFirstName(EmployeeDto.getFirstName());
			DateTime startDate = new DateTime(EmployeeDto.getStartDate());
			engineEmployee.setStartDate(startDate);
			DateTime endDate = new DateTime(EmployeeDto.getEndDate());
			engineEmployee.setStopDate(endDate);
			
			// -1 means seniority is unset
			// This value will be updated in the rule themselves
			engineEmployee.setSeniority(-1);
			
			engineEmployee.setHourlyRate(EmployeeDto.getHourlyRate());

			employeeMap.put(EmployeeDto.getEmployeeID(), engineEmployee);

			convertedEmployees.add(engineEmployee);
		}
		return convertedEmployees;
	}

	public List<WeekendWorkPatternCLDto> convertWeekendPatterns(List<T_EmployeeWeekend> dbWeekendPatterns,
			Map<Long, ContractDto> contracts) {
		List<WeekendWorkPatternCLDto> weekendPatterns = new ArrayList<WeekendWorkPatternCLDto>();
		for (T_EmployeeWeekend t_EmployeeWeekend : dbWeekendPatterns) {

			Long employeeId = t_EmployeeWeekend.getEmployeeID();
			ContractDto contract = contracts.get(employeeId);
			if (contract == null) {
				contract = new ContractDto();
				contract.setId(String.valueOf(employeeId));
				contract.setScope(ContractScope.EmployeeContract);
				contracts.put(employeeId, contract);
			}

			WeekendWorkPatternCLDto weekendPattern = new WeekendWorkPatternCLDto();
			weekendPattern.setContractId(contract.getId());
			Collection<String> daysOffAfter = DBConversionUtils.convertDayBitMapToString(t_EmployeeWeekend
					.getDaysAfter());
			Collection<String> daysOffBefore = DBConversionUtils.convertDayBitMapToString(t_EmployeeWeekend
					.getDaysBefore());

			weekendPattern.setDaysOffAfter(daysOffAfter.toString());
			weekendPattern.setDaysOffBefore(daysOffBefore.toString());

			weekendPattern.setWeight(-1); // TODO: Retrieve from database

			contract.getContractLineDtos().add(weekendPattern);


			// Retrieve shift assignments for from the start of weekend to the 1st shift date of schedule
			DayOfWeek weekendStart = employeeRoster.getWeekendDefinition().getFirstDayOfWeekend();
			ShiftDate firstShiftDate = employeeRoster.getFirstShiftDate();
			int numDaysToLoad = firstShiftDate.getDayOfWeek().getDistanceToPrevious(weekendStart);
			
			ShiftDate weekendDate = firstShiftDate;
			if(numDaysToLoad > 0){
				List<Date> datesToLoad = new ArrayList<>();
				for(int i = 0; i < numDaysToLoad; i++){
					weekendDate = weekendDate.minusDays(1);
					datesToLoad.add(weekendDate.getDate().toDate());
				}
				List<ProductionShiftSummary> productionShiftAssignments;
				try {
					productionShiftAssignments = databaseLoader.loadEmployeePostedShiftAssignments(employeeId, datesToLoad);
					loadPostedShiftAssignments(productionShiftAssignments);
				} catch (NullEntityManagerException e) {
					e.printStackTrace();
				}
			}
			
		}
		return weekendPatterns;
	}

	public List<EmployeeSkillDto> convertEmployeeSkillsFromDB(Collection<EmployeeSkill> dbEmployeeSkills) {
		List<EmployeeSkillDto> skillProfList = new ArrayList<EmployeeSkillDto>();
		for (EmployeeSkill dbEmployeeSkill : dbEmployeeSkills) {
			EmployeeSkillDto skillProf = new EmployeeSkillDto();

			Long skillId = dbEmployeeSkill.getSkillID();
			SkillDto skillDto = skillsMap.get(skillId);
			if (skillDto == null) {
				skillDto = new SkillDto();
				skillDto.setId(String.valueOf(skillId));
				skillDto.setName(dbEmployeeSkill.getName());
				skillsMap.put(skillId, skillDto);
			}

			skillProf.setSkillId(skillDto.getId());
			skillProf.setSkillLevel(String.valueOf(1)); //TODO: Load from DB?
			skillProf.setEmployeeId(String.valueOf(dbEmployeeSkill.getEmployeeID()));
			skillProf.setPrimarySkill(dbEmployeeSkill.getIsPrimary());

			skillProfList.add(skillProf);
		}
		return skillProfList;
	}

	public List<TimeWindowDto> convertCDAvailabilityFromDB(Collection<T_EmployeeCDAvailability> cdAvailabilityList) {
		List<TimeWindowDto> cdTimeOffList = new ArrayList<TimeWindowDto>();
		for (T_EmployeeCDAvailability cdAvailabilityDB : cdAvailabilityList) {
			CDTimeOffDto cdTimeOff = new CDTimeOffDto();
			cdTimeOff.setEmployeeId(String.valueOf(cdAvailabilityDB.getEmployeeID()));
			
			cdTimeOff.setAllDay(false);
			cdTimeOff.setDayOffStart(new DateTime(cdAvailabilityDB.getAvailabilityDate()));

			// End date is not required
			if (cdAvailabilityDB.getAvailabilityEndDate() != null) {
				cdTimeOff.setDayOffEnd(new DateTime(cdAvailabilityDB.getAvailabilityEndDate()));
			}

			// Convert start/end times
			LocalTime startTime = DBConversionUtils.convertDBMilitaryTime(cdAvailabilityDB.getStartTime());
			
			LocalTime endTime   = DBConversionUtils.convertDBMilitaryTime(cdAvailabilityDB.getEndTime());
			
			// Null start/stop values indicate all day event
			if (startTime == null) {
				startTime = LocalTime.MIDNIGHT;
			}

			if (endTime == null || endTime.equals(LocalTime.MIDNIGHT)) {
				endTime = new LocalTime(23, 59, 59);
				if(startTime.equals(LocalTime.MIDNIGHT)){
					cdTimeOff.setAllDay(true); // Its only all day if start at midnight end at 23:59
				}
			}
			
			
			cdTimeOff.setStartTime(startTime);
			cdTimeOff.setEndTime(endTime);

			cdTimeOff.setPTO((cdAvailabilityDB.getAbscenceTypeID() != null));
			
			// This is an unavailability window and can be added directly
			if (cdAvailabilityDB.getAvailabilityStatus() == 1) {
				cdTimeOffList.add(cdTimeOff);
			} else { // This is an availability window that must be converted
				// Create a second unavailability window
				CDTimeOffDto secondUnavailabilityWindow = new CDTimeOffDto(cdTimeOff);
				secondUnavailabilityWindow.setStartTime(secondUnavailabilityWindow.getEndTime());
				secondUnavailabilityWindow.setEndTime(new LocalTime(23, 59, 59));
				if(!secondUnavailabilityWindow.getStartTime().equals(secondUnavailabilityWindow.getEndTime())){
					cdTimeOffList.add(secondUnavailabilityWindow);
				}
				
				// Convert the original availability window to serve as the 1st
				// unavailability window of the day
				cdTimeOff.setEndTime(cdTimeOff.getStartTime());
				cdTimeOff.setStartTime(LocalTime.MIDNIGHT);
				if(!cdTimeOff.getStartTime().equals(cdTimeOff.getEndTime())){
					cdTimeOffList.add(cdTimeOff);
				}
			}
		}
		return cdTimeOffList;
	}
	
	public List<TimeWindowDto> convertCDPreferenceFromDB(Collection<T_EmployeeCDPreference> cdAvailabilityList) {
		List<TimeWindowDto> cdTimeOffList = new ArrayList<TimeWindowDto>();
		for (T_EmployeeCDPreference cdPreferenceDB : cdAvailabilityList) {
			CDPreferenceDto cdPreferenceDto = new CDPreferenceDto();
			cdPreferenceDto.setEmployeeId(String.valueOf(cdPreferenceDB.getEmployeeID()));
			
			cdPreferenceDto.setAllDay(false);
			cdPreferenceDto.setDayOffStart(new DateTime(cdPreferenceDB.getPreferenceDate()));

			// Convert start/end times
			LocalTime startTime = DBConversionUtils.convertDBMilitaryTime(cdPreferenceDB.getStartTime());
			LocalTime endTime   = DBConversionUtils.convertDBMilitaryTime(cdPreferenceDB.getEndTime());
			
			// Null start/stop values indicate all day event
			if (startTime == null) {
				startTime = LocalTime.MIDNIGHT;
			}

			if (endTime == null || endTime.equals(LocalTime.MIDNIGHT)) {
				endTime = new LocalTime(23, 59, 59);
				if(startTime.equals(LocalTime.MIDNIGHT)){
					cdPreferenceDto.setAllDay(true); // Its only all day if start at midnight end at 23:59
				}
			}
			
			cdPreferenceDto.setStartTime(startTime);
			cdPreferenceDto.setEndTime(endTime);

			// If PreferenceStatus = 1, employee wants to avoid that day
			if (cdPreferenceDB.getPreferenceStatus() == 1) {
				cdPreferenceDto.setType(PreferenceType.PreferedUnavail);
				cdPreferenceDto.setWeight(-1);
			} else {
				cdPreferenceDto.setType(PreferenceType.PreferedAvail);
				cdPreferenceDto.setWeight(1);
			}
			
			cdTimeOffList.add(cdPreferenceDto);
		}
		return cdTimeOffList;
	}


	public List<TimeWindowDto> convertCIAvailabilityFromDB(Collection<T_EmployeeCIAvailability> ciAvailabilityList) {
		List<TimeWindowDto> ciTimeOffList = new ArrayList<TimeWindowDto>();
		for (T_EmployeeCIAvailability ciAvailabilityDB : ciAvailabilityList) {
			CITimeOffDto ciTimeOff = new CITimeOffDto();
			ciTimeOff.setEmployeeId(String.valueOf(ciAvailabilityDB.getEmployeeID()));
			ciTimeOff.setAllDay(false);

			// TODO: Update with better handing of start of week in DB
			ciTimeOff.setDayOfWeek(valueOfCalendar(ciAvailabilityDB.getWeekdayNumber()));

			// Convert start/end times
			LocalTime startTime = DBConversionUtils.convertDBMilitaryTime(ciAvailabilityDB.getStartTime());
			MutableBoolean endOfDay = new MutableBoolean(false); // In CI this is only true if end time == 2400
			LocalTime endTime = DBConversionUtils.convertDBMilitaryTime(ciAvailabilityDB.getEndTime());

			// Null start/stop values indicate all day event
			if (startTime == null) {
				startTime = LocalTime.MIDNIGHT;
			}

			if (endTime == null || endOfDay.booleanValue()) {
				endTime = new LocalTime(23, 59, 59);
				if(startTime == LocalTime.MIDNIGHT){
					ciTimeOff.setAllDay(true); // Its only all day if start at midnight end at 23:59
				}
			}

			ciTimeOff.setStartTime(startTime);
			ciTimeOff.setEndTime(endTime);

			// This is an unavailability window and can be added directly
			if(ciAvailabilityDB.getAvailabilityStatus() == 1){
				ciTimeOffList.add(ciTimeOff);
			} else { // This is an availability window that must be converted
				// Create a second unavailability window
				CITimeOffDto secondUnavailabilityWindow = new CITimeOffDto(ciTimeOff);
				secondUnavailabilityWindow.setStartTime(secondUnavailabilityWindow.getEndTime());
				secondUnavailabilityWindow.setEndTime(new LocalTime(23, 59, 59));
				if(!secondUnavailabilityWindow.getStartTime().equals(secondUnavailabilityWindow.getEndTime())){
					ciTimeOffList.add(secondUnavailabilityWindow);
				}
				
				//Convert the original availability window to serve as the 1st unavailability window of the day
				ciTimeOff.setEndTime(ciTimeOff.getStartTime());
				ciTimeOff.setStartTime(LocalTime.MIDNIGHT);
				if(!ciTimeOff.getStartTime().equals(ciTimeOff.getEndTime())){
					ciTimeOffList.add(ciTimeOff);
				}
			}
		}
		return ciTimeOffList;
	}
	
	public List<TimeWindowDto> convertCIPreferenceFromDB(Collection<T_EmployeeCIPreference> ciPreferenceList) {
		List<TimeWindowDto> ciTimeOffList = new ArrayList<TimeWindowDto>();
		for (T_EmployeeCIPreference ciPreferenceDB : ciPreferenceList) {
			CIPreferenceDto ciPreferenceDto = new CIPreferenceDto();
			ciPreferenceDto.setEmployeeId(String.valueOf(ciPreferenceDB.getEmployeeID()));
			ciPreferenceDto.setAllDay(false);

			// TODO: Update with better handing of start of week in DB
			ciPreferenceDto.setDayOfWeek(valueOfCalendar(ciPreferenceDB.getWeekdayNumber()));

			// Convert start/end times
			LocalTime startTime = DBConversionUtils.convertDBMilitaryTime(ciPreferenceDB.getStartTime());
			MutableBoolean endOfDay = new MutableBoolean(false); // In CI this is only true if end time == 2400
			LocalTime endTime = DBConversionUtils.convertDBMilitaryTime(ciPreferenceDB.getEndTime(), endOfDay);

			// Null start/stop values indicate all day event
			if (startTime == null) {
				startTime = LocalTime.MIDNIGHT;
			}

			if (endTime == null || endOfDay.booleanValue()) {
				endTime = new LocalTime(23, 59, 59);
				if(startTime == LocalTime.MIDNIGHT){
					ciPreferenceDto.setAllDay(true); // Its only all day if start at midnight end at 23:59
				}
			}

			ciPreferenceDto.setStartTime(startTime);
			ciPreferenceDto.setEndTime(endTime);
			
			// If PreferenceStatus = 1, employee wants to avoid that day
			if (ciPreferenceDB.getPreferenceStatus() == 1) {
				ciPreferenceDto.setType(PreferenceType.PreferedUnavail);
				ciPreferenceDto.setWeight(-1);
			} else {
				ciPreferenceDto.setType(PreferenceType.PreferedAvail);
				ciPreferenceDto.setWeight(1);
			}

			ciTimeOffList.add(ciPreferenceDto);

			
		}
		return ciTimeOffList;
	}
	

	public void convertCIRotationContractsFromDB(Collection<T_EmployeeCIRotation> ciRotationList,
			Map<Long, ContractDto> contracts) {
		for (T_EmployeeCIRotation ciRotationDB : ciRotationList) {
			WeekdayRotationPatternCLDto ciWeekdayRotationPattern = new WeekdayRotationPatternCLDto();
			ciWeekdayRotationPattern.setWeight(-1); //TODO: Load from DB
			
			long employeeId = ciRotationDB.getEmployeeID();
			ContractDto contract = contracts.get(employeeId);
			if (contract == null) {
				contract = new ContractDto();
				contract.setId(String.valueOf(employeeId));
				contract.setScope(ContractScope.EmployeeContract);
				contracts.put(employeeId, contract);
			}

			ciWeekdayRotationPattern.setDayOfWeek(valueOfCalendar(ciRotationDB.getWeekdayNumber()));

			// TODO: In future versions this will be loaded from db
			ciWeekdayRotationPattern.setRotationType(RotationPatternType.DAYS_OFF_PATTERN);

			// Convert database rotation type to a list of days of week
			selectCIRotationPattern(ciRotationDB.getRotationValue(), ciWeekdayRotationPattern);
			
			loadPostedShiftAssignments(employeeId, ciWeekdayRotationPattern);
			
			contract.getContractLineDtos().add(ciWeekdayRotationPattern);
		}
	}
	
	private void loadPostedShiftAssignments(Long employeeID, WeekdayRotationPatternCLDto rotationPattern){
		int totalDays = rotationPattern.getOutOfTotalDays();
		DayOfWeek rotationDay = rotationPattern.getDayOfWeek();
		
		List<Date> datesToLoad = new ArrayList<Date>();
		EmployeeRosterDto info = startingSolution.getEmployeeRosterDto();
		ShiftDate firstDayOfWeek = info.getPlanningWindowStart().getDateOfFirstDayOfWeek(rotationDay);
		LocalDate dateToAdd = firstDayOfWeek.getDate();
		while(dateToAdd.isAfter(firstDayOfWeek.getDate().minusDays(7*totalDays))){
			datesToLoad.add(dateToAdd.toDate());
			dateToAdd = dateToAdd.minusDays(7);
		}
		
		try {
			List<ProductionShiftSummary> productionShiftAssignments = databaseLoader.loadEmployeePostedShiftAssignments(employeeID, datesToLoad);
			loadPostedShiftAssignments(productionShiftAssignments);
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
			return;
		}
	}

	private void loadPostedShiftAssignments(List<ProductionShiftSummary> productionShiftAssignments){
		for(ProductionShiftSummary prodShift : productionShiftAssignments){
			ShiftDto shiftDto = new ShiftDto();
			shiftDto.setRequiredEmployeeSize(1);
			shiftDto.setId(String.valueOf(prodShift.getSiteRequirementID()));
			shiftDto.setEndDateTime(shiftDto.getStartDateTime());
			
			LocalDate startDate = new LocalDate(prodShift.getShiftDate());
			LocalTime startTime = DBConversionUtils.convertDBMilitaryTime(prodShift.getStartTime());
			DateTime startLocalDt = new DateTime(startDate).plusHours(startTime.getHourOfDay()).plusMinutes(startTime.getMinuteOfHour());
			shiftDto.setStartDateTime(startLocalDt);
			
			LocalDate endDate = new LocalDate(prodShift.getShiftDate());
			LocalTime stopTime = DBConversionUtils.convertDBMilitaryTime(prodShift.getEndTime());
			DateTime stopLocalDt = new DateTime(endDate).plusHours(stopTime.getHourOfDay()).plusMinutes(stopTime.getMinuteOfHour());
			
			shiftDto.setStartDateTime(stopLocalDt);
			
			startingSolution.getShiftDtos().add(shiftDto);
			
			ShiftAssignmentDto assignment = new ShiftAssignmentDto();
			assignment.setEmployeeId(String.valueOf(prodShift.getEmployeeID()));
			assignment.setShiftId(shiftDto.getId());
			
			assignment.setLocked(true);
			startingSolution.getShiftAssignmentDtos().add(assignment);
		}
	}
	
	/**
	 * Aspen database has three patterns hard coded to a integer value this
	 * methods sets the pattern type based on the value stored in the database
	 * 
	 * @param rotationValue
	 * @param pattern
	 * @throws IllegalArgumentException
	 */
	protected void selectCIRotationPattern(int rotationValue, WeekdayRotationPatternCLDto pattern)
			throws IllegalArgumentException {
		switch (rotationValue) {
		case 2:
			pattern.setNumberOfDays(1);
			pattern.setOutOfTotalDays(2);
			break;
		case 3:
			pattern.setNumberOfDays(1);
			pattern.setOutOfTotalDays(3);
			break;
		case 4:
			pattern.setNumberOfDays(2);
			pattern.setOutOfTotalDays(4);
			break;
		default:
			throw new IllegalArgumentException("The rotation pattern (" + rotationValue + ") is not supported.");
		}
	}

	public void convertSiteContractFromDB(Collection<SiteContractLine> dbContractLineList, Map<Long, ContractDto> contracts) {
		for (SiteContractLine dbContractLine : dbContractLineList) {
			for(Long teamId : teamIds){
				String restrictionName = dbContractLine.getRestrictionName();
				
				ContractDto contract = new ContractDto();
				contract.setScope(ContractScope.TeamContract);
				contract.setId(String.valueOf(teamId));
				IntMinMaxCLDto contractLine = createMinMaxContractLine(contract, restrictionName, (int)dbContractLine.getMaxValue(), (int)dbContractLine.getMinValue(),
						-1, -1);
				
				// All constraints except for one are hours expressed as decimals and need to be converted
				// to minutes.
				if(contractLine != null && !restrictionName.equals(ContractLineType.CONSECUTIVE_WORKING_DAYS.getValue())
						&& !restrictionName.equals(ContractLineType.OVERLAPPING_SHIFTS.getValue())){
					int maxValue = DBConversionUtils.convertDecimalHoursToMinutes(dbContractLine.getMaxValue());
					int minValue = DBConversionUtils.convertDecimalHoursToMinutes(dbContractLine.getMinValue());
					
					contractLine.setMaximumValue(maxValue);
					contractLine.setMinimumValue(minValue);
				} 

				if (contractLine != null && contractLine.isMaximumEnabled() && contractLine.isMinimumEnabled()) {
					contract.getContractLineDtos().add(contractLine);
				}
			}
		}
	}

	public List<ContractDto> convertEmployeeContractLinesFromDB(Collection<EmployeeContractLine> dbContractLineList,
			Map<Long, ContractDto> contracts) {
		for (EmployeeContractLine dbContractLine : dbContractLineList) {
			Long employeeId = dbContractLine.getEmployeeId();
			ContractDto contract = contracts.get(employeeId);
			if (contract == null) {
				contract = new ContractDto();
				contract.setId(String.valueOf(employeeId));
				contract.setScope(ContractScope.EmployeeContract);
				contracts.put(employeeId, contract);
			}

			String restrictionName = dbContractLine.getRestrictionName();
			IntMinMaxCLDto contractLine = createMinMaxContractLine(contract, restrictionName, (int)dbContractLine.getMaxValue(), (int)dbContractLine.getMinValue(),
					-1, -1);
			
			// All constraints except for two are hours expressed as decimals and need to be converted
			// to minutes.
			if(contractLine != null && !restrictionName.equals(ContractLineType.CONSECUTIVE_WORKING_DAYS.getValue())
					&& !restrictionName.equals(ContractLineType.OVERLAPPING_SHIFTS.getValue())){
				int maxValue = DBConversionUtils.convertDecimalHoursToMinutes(dbContractLine.getMaxValue());
				int minValue = DBConversionUtils.convertDecimalHoursToMinutes(dbContractLine.getMinValue());
				
				contractLine.setMaximumValue(maxValue);
				contractLine.setMinimumValue(minValue);
			} 
			
			// No need to add a contract line if it is disabled
			if (contractLine != null && contractLine.isMaximumEnabled() && contractLine.isMinimumEnabled()) {
				contract.getContractLineDtos().add(contractLine);
			}
		}

		return new ArrayList<ContractDto>(employeeContracts.values());
	}

	private IntMinMaxCLDto createMinMaxContractLine(ContractDto contract, String restrictionName, int maxValue,
			int minValue, int maxWeight, int minWeight) {
		IntMinMaxCLDto contractLine = new IntMinMaxCLDto();
		ContractLineType type = ContractLineType.fromString(restrictionName);
		contractLine.setContractLineType(type);

		if (minValue > DISABLED_MIN_VALUE) {
			contractLine.setMinimumValue(minValue);
			contractLine.setMinimumEnabled(true);
			contractLine.setMinimumWeight(minWeight);
		}

		if (maxValue < DISABLED_MAX_VALUE && maxValue > DISABLED_MIN_VALUE) {
			contractLine.setMaximumValue(maxValue);
			contractLine.setMaximumEnabled(true);
			contractLine.setMaximumWeight(maxWeight); // TODO: Should retrieve
														// this from DB
		}

		if (contractLine.isMaximumEnabled() || contractLine.isMinimumEnabled()) {
			contractLine.setContractId(contract.getId());
			return contractLine;
		}
		return null;
	}

	public List<ShiftDto> convertShiftDemandFromDB(Collection<ShiftDemand> shiftDemands) {
		List<ShiftDto> shifts = new ArrayList<ShiftDto>();
		for (ShiftDemand shiftDemand : shiftDemands) {
			ShiftDto shiftDto = new ShiftDto();
			shiftDto.setRequiredEmployeeSize(1); // TODO: Remove required EmployeeDto size complete
			shiftDto.setId(String.valueOf(shiftDemand.getSiteRequirementID()));

			LocalDate demandDate = new LocalDate(shiftDemand.getDemandDate());

			//If start time == 2400 we should move the start date forward by 1
			MutableBoolean shiftStartsAtNextDay = new MutableBoolean(false);
			LocalTime shiftStartTime = DBConversionUtils.convertDBMilitaryTime(shiftDemand.getStartingTime(), shiftStartsAtNextDay);
			
			MutableBoolean shiftSpillsOverDay = new MutableBoolean(false);
			LocalTime shiftEndTime = DBConversionUtils.convertDBMilitaryTime(shiftDemand.getEndingTime(),
					shiftSpillsOverDay);

			ShiftDate shiftStartDate = new ShiftDate(demandDate);
			shiftDateList.add(shiftStartDate);

			LocalDate shiftEndLocalDate = demandDate;

			// If the ShiftDto end time is >= 2400 and the ShiftDto start time < 2400 the ShiftDto spills over to the next day
			if (shiftSpillsOverDay.booleanValue() && !shiftStartsAtNextDay.booleanValue()) {
				shiftEndLocalDate = shiftEndLocalDate.plusDays(1); 
			}

			ShiftDate shiftEndDate = new ShiftDate(shiftEndLocalDate);
			shiftDateList.add(shiftEndDate);
			
			DateTime demandLocalDt = new DateTime(demandDate).plusHours(shiftStartTime.getHourOfDay()).plusMinutes(shiftStartTime.getMinuteOfHour());
			DateTime shiftEndLocalDt = new DateTime(shiftEndLocalDate).plusHours(shiftEndTime.getHourOfDay()).plusMinutes(shiftEndTime.getMinuteOfHour());
			
			shiftDto.setStartDateTime(demandLocalDt);
			shiftDto.setEndDateTime(shiftEndLocalDt);
			
			shiftDto.setTeamId(String.valueOf(shiftDemand.getTeamID()));
			boolean isExcessShift = (shiftDemand.getShiftType() == 1) ? false : true;
			shiftDto.setExcessShift(isExcessShift);

			long skillCode = shiftDemand.getSkillID();
			shiftDto.setSkillId(String.valueOf(skillCode));
			SkillDto shiftSkill = skillsMap.get(skillCode);
			if (shiftSkill == null) {
				String skillName = shiftDemand.getSkillName();
				shiftSkill = new SkillDto();
				shiftSkill.setId(String.valueOf(skillCode));
				shiftSkill.setName(skillName);
				
				skillsMap.put(skillCode, shiftSkill);
			}

			shifts.add(shiftDto);
		}

		return shifts;

	}

	public List<TimeWindowDto> getTimeOffForSchedule() {
		try {
			List<T_EmployeeCDAvailability> dbCDEmployeeAvailibility = databaseLoader.loadTeamCDAvailability(teamIds,
					planningStartDate, scheduleEndDate);
			List<T_EmployeeCDPreference>   dbCDEmployeePreference   = databaseLoader.loadTeamCDPreference(teamIds,
					planningStartDate, scheduleEndDate);
			
			List<T_EmployeeCIAvailability> dbCIEmployeeAvailability = databaseLoader.loadTeamCIAvailability(teamIds);
			List<T_EmployeeCIPreference> dbCIEmployeePreference = databaseLoader.loadTeamCIPreference(teamIds);

			List<TimeWindowDto> timeOffDto = convertCDAvailabilityFromDB(dbCDEmployeeAvailibility);
			timeOffDto.addAll(convertCDPreferenceFromDB(dbCDEmployeePreference));
			
			timeOffDto.addAll(convertCIAvailabilityFromDB(dbCIEmployeeAvailability));
			timeOffDto.addAll(convertCIPreferenceFromDB(dbCIEmployeePreference));

			return timeOffDto;
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<EmployeeDto> getEmployeesForTeam() {
		try {
			List<T_Employee> dbEmployees = databaseLoader.loadEmployeesForTeam(teamIds);
			return convertEmployeesFromDB(dbEmployees);
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<EmployeeTeamDto> convertEmployeeTeamAssociationsFromDB(
			List<EmployeeIDToTeamID> dbEmployeeTeamAssociations) throws IncorrectDataLoadingOrderException {
		if (employeeMap.isEmpty()) {
			throw new IncorrectDataLoadingOrderException("EmployeeDto List", "EmployeeDto Team Associations");
		}

		List<EmployeeTeamDto> teamAssociations = new ArrayList<EmployeeTeamDto>();
		for(EmployeeIDToTeamID dbTeamAssoc : dbEmployeeTeamAssociations){
			EmployeeTeamDto teamAssoc = new EmployeeTeamDto();
			teamAssoc.setEmployeeId(String.valueOf(dbTeamAssoc.getEmployeeID()));
			teamAssoc.setTeamId(String.valueOf(dbTeamAssoc.getTeamID()));
			teamAssoc.setType(dbTeamAssoc.getTeamStatusValue() == 0 ? TeamAssociationType.ON : TeamAssociationType.FLOAT);
			teamAssociations.add(teamAssoc);
		}

		return teamAssociations;
	}

	private List<EmployeeTeamDto> getEmployeeTeamAssociations() {
		try {
			List<EmployeeIDToTeamID> dbEmployeeTeamAssociations = databaseLoader
					.loadEmployeeToTeamRelationships(teamIds);
			return convertEmployeeTeamAssociationsFromDB(dbEmployeeTeamAssociations);
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		} catch (IncorrectDataLoadingOrderException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<ShiftDto> getShiftDemandForTeam() {
		try {
			List<ShiftDemand> dbShiftDemands = databaseLoader.loadTeamScheduleShiftDemand(teamIds, planningStartDate,
					scheduleEndDate);
			System.out.println("Open Shifts: " + dbShiftDemands.size());
			return convertShiftDemandFromDB(dbShiftDemands);
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<EmployeeSkillDto> getEmployeeSkillProficiency() {
		try {
			List<EmployeeSkill> dbEmployeeSkills = databaseLoader.loadTeamEmployeeSkills(teamIds);
			return convertEmployeeSkillsFromDB(dbEmployeeSkills);
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<SkillDto> getRequiredSkills() {
		return new ArrayList<SkillDto>(skillsMap.values()); // TODO: Must be a
															// better way
	}

	private List<ShiftSkillRequirement> getShiftTypeSkillsReqs() {
		return shiftSkillRequirementsList;
	}

	private void loadAllContracts() {
		try {
			List<SiteContractLine> dbSiteContractLines = databaseLoader.loadSiteContractLines(siteId);
			List<T_EmployeeCIRotation> dbCIRotationList = databaseLoader.loadTeamCIRotation(teamIds);
			List<EmployeeContractLine> dbEmployeeContractLines = databaseLoader.loadTeamEmployeeContractLines(teamIds);
			List<T_EmployeeWeekend> dbEmployeeWeekendOptions = databaseLoader.loadTeamWeekendOptions(teamIds);

			Map<Long, ContractDto> contracts = new HashMap<Long, ContractDto>();
			convertSiteContractFromDB(dbSiteContractLines, contracts);
			convertEmployeeContractLinesFromDB(dbEmployeeContractLines, contracts);
			convertCIRotationContractsFromDB(dbCIRotationList, contracts);
			convertWeekendPatterns(dbEmployeeWeekendOptions, contracts);

			startingSolution.setContractDtos(new ArrayList<ContractDto>(contracts.values()));
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
	}
	
	public List<ConstraintOverrideDto> getConstraintOverrides(){
		List<ConstraintOverrideDto> constraintOverrides = new ArrayList<ConstraintOverrideDto>();
		try {
			List<T_SiteResource> dbConstraintOverrides = databaseLoader.loadEmployeeConstraintOverrides(scheduleId);
			for(T_SiteResource t : dbConstraintOverrides){
				ConstraintOverrideDto co = convertRTOToConstraintOverride(t);
				if(co != null) {
					constraintOverrides.add(co);
				}
			}
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		return constraintOverrides;
	}
	
	public List<ConstraintOverrideDto> getConstraintOverrideDTOs(){
		List<ConstraintOverrideDto> constraintOverrides = new ArrayList<ConstraintOverrideDto>();
		try {
			List<T_SiteResource> dbConstraintOverrides = databaseLoader.loadEmployeeConstraintOverrides(scheduleId);
			for(T_SiteResource t : dbConstraintOverrides){
				ConstraintOverrideDto co = convertRTOToConstraintOverrideDto(t);
				if(co != null) {
					constraintOverrides.add(co);
				}
			}
		} catch (NullEntityManagerException e) {
			e.printStackTrace();
		}
		return constraintOverrides;
	}
	
	public ConstraintOverrideDto convertRTOToConstraintOverrideDto(T_SiteResource rto){
		ConstraintOverrideDto override = new ConstraintOverrideDto();
		EmployeeDto emp = employeeMap.get(rto.getEmployeeId());
		if(emp == null) return null;
		override.setEmployeeId(emp.getId());
		ConstraintOverrideType type = getConstraintOverrideType(rto.getRtoBinaryTally());
		if(type == null) return null;
		
		// Special case for EmployeeDto not schedulable
		if(type == ConstraintOverrideType.EMPLOYEE_UNAVAILABLE){
			emp.setScheduleable(false);
			return null; // Constraint is built in no need to return object
		}
		
		override.setType(type);
		return override;
	}
	
	public ConstraintOverrideDto convertRTOToConstraintOverride(T_SiteResource rto){
		ConstraintOverrideDto override = new ConstraintOverrideDto();
		EmployeeDto emp = employeeMap.get(rto.getEmployeeId());
		if(emp == null) return null;
		override.setEmployeeId(emp.getId());
		ConstraintOverrideType type = getConstraintOverrideType(rto.getRtoBinaryTally());
		if(type == null) return null;
		
		// Special case for EmployeeDto not schedulable
		if(type == ConstraintOverrideType.EMPLOYEE_UNAVAILABLE){
			emp.setScheduleable(false);
			return null; // Constraint is built in no need to return object
		}
		
		override.setType(type);
		return override;
	}

	public ConstraintOverrideType getConstraintOverrideType(long rtoBitmask){
		if(rtoBitmask == ENABLE_FLOAT) return ConstraintOverrideType.TEAM_FLOAT_ON;
		if(rtoBitmask == IGNORE_COUPLED_WEEKENDS) return ConstraintOverrideType.COUPLED_WEEKEND_OVERRIDE;
		if(rtoBitmask == IGNORE_DAILY_TIME_LIMIT) return ConstraintOverrideType.TIME_WINDOW_UNAVAILABLE_OVERRIDE;
		if(rtoBitmask == IGNORE_DAY_ROTATION) return ConstraintOverrideType.WEEKDAY_ROTATION_OVERRIDE;
		if(rtoBitmask == IGNORE_DAYS_AFTER) return ConstraintOverrideType.DAYS_OFF_AFTER_OVERRIDE;
		if(rtoBitmask == IGNORE_DAYS_BEFORE) return ConstraintOverrideType.DAYS_OFF_BEFORE_OVERRIDE;
		if(rtoBitmask == IGNORE_MAX_CONSECUTIVE_DAYS) return ConstraintOverrideType.MAX_CONSECUTIVE_DAYS_OVERRIDE;
		if(rtoBitmask == IGNORE_MAX_DAYS_PER_WEEK) return ConstraintOverrideType.MAX_DAYS_WEEK_OVERRIDE;
		if(rtoBitmask == IGNORE_MAX_HOURS_PER_DAY) return ConstraintOverrideType.MAX_HOURS_DAY_OVERRIDE;
		if(rtoBitmask == IGNORE_MAX_HOURS_PER_WEEK) return ConstraintOverrideType.MAX_HOURS_WEEK_OVERRIDE;
		if(rtoBitmask == IGNORE_MIN_HOURS_BETWEEN_DAYS) return ConstraintOverrideType.MIN_HOURS_BETWEEN_DAYS_OVERRIDE;
		if(rtoBitmask == IGNORE_MIN_HOURS_PER_DAY) return ConstraintOverrideType.MIN_HOURS_DAY_OVERRIDE;
		if(rtoBitmask == IGNORE_MIN_HOURS_PER_WEEK) return ConstraintOverrideType.MIN_HOURS_WEEK_OVERRIDE;
		if(rtoBitmask == IGNORE_PAID_TIME_OFF) return ConstraintOverrideType.PTO_OVERRIDE;
		if(rtoBitmask == IGNORE_UNAVAILABLE) return ConstraintOverrideType.ALL_DAY_UNAVAILABLE_OVERRIDE;
		if(rtoBitmask == AVOID_SCHEDULING) return ConstraintOverrideType.EMPLOYEE_UNAVAILABLE;
		if(rtoBitmask == AVOID_OVERTIME) return ConstraintOverrideType.AVOID_OVERTIME;

		return null;
	}
	
	
	public AssignmentRequestDto getStartingSchedule() {
		loadSiteInformation();

		List<EmployeeDto> employees = getEmployeesForTeam();
		startingSolution.setEmployeeDtos(employees);

		List<ShiftDto> shifts = getShiftDemandForTeam();
		startingSolution.setShiftDtos(shifts);

		List<EmployeeSkillDto> skillProfList = getEmployeeSkillProficiency();
		startingSolution.setEmployeeSkillDtos(skillProfList);

		List<TimeWindowDto> employeeTimeOff = getTimeOffForSchedule();
		
		for(final EmployeeDto employee : employees){
			Collection<TimeWindowDto> employeesPTO = CollectionUtils.select(employeeTimeOff, new Predicate<TimeWindowDto>(){
				@Override
				public boolean evaluate(TimeWindowDto object) {
					return employee.getId().equals(object.getEmployeeId());
				}
			});
			
			employee.setEmployeeTimeOffDtos(new ArrayList(employeesPTO));
		}
		
		loadAllContracts();

		startingSolution.setSkillDtos(getRequiredSkills());

		List<EmployeeTeamDto> teamAssociations = getEmployeeTeamAssociations();
		startingSolution.setEmployeeTeamDtos(teamAssociations);
		
		startingSolution.setConstraintOverrideDtos(getConstraintOverrideDTOs());

		// Load existing ShiftDto assignments for weekday rotations
		//TODO: These need to be included 
		//startingSolution.setShiftDateList(new ArrayList<ShiftDate>(shiftDateList));
		startingSolution.setEmployeeRosterDto(employeeRoster);
		
		startingSolution.setShiftAssignmentDtos(Collections.<ShiftAssignmentDto>emptyList());
		
		return startingSolution;
	}

	/**
	 * Creates a ShiftDto date object for every weekend day in schedule to be used
	 * as a reference anchor in certain rules that measure a quantity per week.
	 * 
	 * @param scheduleStartShiftDate
	 */
	private void propagateWeekendShiftDates(ShiftDate scheduleStartShiftDate, ShiftDate scheduleEndShiftDate) {
		ShiftDate nextWeekendShiftDate = scheduleStartShiftDate;

		do {
			nextWeekendShiftDate = new ShiftDate(nextWeekendShiftDate.getDate().plusDays(7));
			shiftDateList.add(nextWeekendShiftDate);
		} while (nextWeekendShiftDate.isBefore(scheduleEndShiftDate));
	}

	/**
	 * Different database conventions and different libraries define the
	 * numerical value of the day of the week differently.
	 * 
	 * Therefore the translation to DayOfWeek should be left up to specific
	 * interfaces.
	 * 
	 * @param calendarDayInWeek
	 * @return
	 */
	public DayOfWeek valueOfCalendar(int calendarDayInWeek) {
		switch (calendarDayInWeek) {
		case 0:
			return DayOfWeek.SUNDAY;
		case 1:
			return DayOfWeek.MONDAY;
		case 2:
			return DayOfWeek.TUESDAY;
		case 3:
			return DayOfWeek.WEDNESDAY;
		case 4:
			return DayOfWeek.THURSDAY;
		case 5:
			return DayOfWeek.FRIDAY;
		case 6:
			return DayOfWeek.SATURDAY;
		default:
			throw new IllegalArgumentException("The calendarDayInWeek (" + calendarDayInWeek + ") is not supported.");
		}
	}
}
