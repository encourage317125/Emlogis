package com.emlogis.common.services.employee;

import com.emlogis.common.Constants;
import com.emlogis.common.TimeUtil;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.CacheConstants;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;
import com.emlogis.model.employee.CDAvailabilityTimeFrame;
import com.emlogis.model.employee.Employee;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import com.emlogis.server.services.cache.BasicCacheService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Minutes;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class CDAvailabilityTimeFrameService {
	
    private final static Logger logger = Logger.getLogger(ScheduleService.class);

    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
	
    @EJB
    private ScheduleService scheduleService;

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    @EJB
    private BasicCacheService cacheService;

    /**
	 * findCDAvailabilityTimeFrames() find a list of CDAvailabilityTimeFrames matching criteria;
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<CDAvailabilityTimeFrame> findCDAvailabilityTimeFrames(SimpleQuery sq)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		sq.setEntityClass(CDAvailabilityTimeFrame.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, sq);
	}	
	
	/**
	 * Get availabilityTimeFrame 
	 * @param primaryKey
	 * @return
	 */
	public CDAvailabilityTimeFrame getCDAvailabilityTimeFrame(PrimaryKey primaryKey) {
		return entityManager.find(CDAvailabilityTimeFrame.class, primaryKey);
	}

	/**
	 * Create CDAvailabilityTimeFrameService
	 * @param primaryKey
	 * @param employee
	 * @param absenceType
	 * @param reason
	 * @param duration
	 * @param availabilityType
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
    public CDAvailabilityTimeFrame createCDAvailabilityTimeFrame(PrimaryKey primaryKey, Employee employee,
			AbsenceType absenceType, String reason, Minutes duration, AvailabilityType availabilityType,
            DateTime startDateTime, boolean isPTO) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, ValidationException {
		CDAvailabilityTimeFrame availabilityTimeFrame = new CDAvailabilityTimeFrame(primaryKey, employee, absenceType,
                reason, duration, availabilityType, startDateTime, isPTO);
		validateTimeFrame(availabilityTimeFrame);
		entityManager.persist(availabilityTimeFrame);
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, employee.getTenantId(), employee.getId());
		return availabilityTimeFrame;
	}  
    
    /**
     * Update availabilityTimeFrame
     * @param availabilityTimeFrame
     * @return
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public CDAvailabilityTimeFrame update(CDAvailabilityTimeFrame availabilityTimeFrame) 
    		throws InstantiationException, IllegalAccessException, InvocationTargetException , ValidationException{
        availabilityTimeFrame.touch();
		validateTimeFrame(availabilityTimeFrame);
        CDAvailabilityTimeFrame updatedCdAvailabilityTimeFrame = entityManager.merge(availabilityTimeFrame);
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, availabilityTimeFrame.getTenantId(),
                availabilityTimeFrame.getEmployeeId());

        // TODO - Get update to PostedOpenShifts working.  At time of this writing, the update logic is not finding
        //        the updated availability and taking it into account.  Look at transacttion management.
        //        Leaving it commented out for now.
//        // Update the PostedOpenShifts table for any affected schedules.  Note, only doing this here in the 
//        // update method, as it gets called by facade for both create and update.
//        entityManager.flush();  // to make sure the update is persisted/available for engine data prep
//        String tenantId = updatedCdAvailabilityTimeFrame.getTenantId();
//        final String employeeId = updatedCdAvailabilityTimeFrame.getEmployeeId();
//        final ArrayList<String> employeeIds = new ArrayList<String>();
//        employeeIds.add(employeeId);
//
//        Set<String> scheduleIds = postedOpenShiftService.getSchedules(tenantId, employeeId);
//        for (String scheduleId : scheduleIds){
//        	final com.emlogis.model.schedule.Schedule schedule = scheduleService.getSchedule(new PrimaryKey(tenantId, scheduleId));
//
//        	int minutes = updatedCdAvailabilityTimeFrame.getDurationInMinutes().getMinutes();
//        	DateTime cdAvailTimeFrameEndTime = updatedCdAvailabilityTimeFrame.getStartDateTime().plusMinutes(minutes);
//
//        	if (schedule.getStartDate()  <=  cdAvailTimeFrameEndTime.getMillis() && 
//        			schedule.getEndDate()    >=  updatedCdAvailabilityTimeFrame.getStartDateTime().getMillis() ){				
//        		// We'll asynchronously update the PostedOpenShifts table (if needed) 
//        		// so we can proceed with returning to the caller...
//        		new Thread(new Runnable() {
//        			public void run(){
//        				try {
//        					scheduleService.updateScheduleEmployeesPostedOpenShifts(schedule.getPrimaryKey(), employeeIds);								
//        				} catch (IllegalAccessException e) {
//        					logger.info("Exception while checking if any PostedOpenShift table updates are needed"
//        							+ " after most recent availability change involving employee " + employeeId);
//        				}
//        			}
//        		}).start();
//        	}
//        }	

		return updatedCdAvailabilityTimeFrame;
    }   
    
    /**
     * Delete availabilityTimeFrame
     * @param availabilityTimeFrame
     */
    public void delete(CDAvailabilityTimeFrame availabilityTimeFrame) {
    	availabilityTimeFrame.getEmployee().removeAvailabilityTimeFrame(availabilityTimeFrame);
    	entityManager.persist(availabilityTimeFrame.getEmployee());
        entityManager.remove(availabilityTimeFrame);
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, availabilityTimeFrame.getTenantId(),
                availabilityTimeFrame.getEmployeeId());
    }  	
    
    /**
	 * Checks that this the given calendar dependent time frame doesn't span
	 * across midnight and doesn't overlap any other calendar dependent
	 * time frame for the associated employee.
     * 
     * @param availabilityTimeFrame
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ValidationException
     */
	private void validateTimeFrame(CDAvailabilityTimeFrame availabilityTimeFrame) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, ValidationException {
		// Check that this date won't span multiple days
		DateTime startDateTime = availabilityTimeFrame.getStartDateTime();
		
		// Check that PTO designation is only for a full day unavailability
		AvailabilityType availType = availabilityTimeFrame.getAvailabilityType();
		// TODO: Investigate if this could be made DST aware
		boolean fullDay = availabilityTimeFrame.getDurationInMinutes().getMinutes() >= Constants.MIN_MINUTES_PER_DAY;
		if (availabilityTimeFrame.getIsPTO() && (!fullDay || availType.equals(AvailabilityType.Avail))) {
			throw new ValidationException("PTO is only supported for full days of unavailability");
		}
	
		Minutes duration = availabilityTimeFrame.getDurationInMinutes();
		// TODO: Investigate if this could be made DST aware
		if (duration.getMinutes() > Constants.MAX_MINUTES_PER_DAY) {
			throw new ValidationException("An availability timeframe cannot span multiple days");
		}
		
		// Check this timeframe won't overlap with any existing CD time frames for this employee
		
		String tenantId = availabilityTimeFrame.getTenantId();
		SimpleQuery simpleQuery = new SimpleQuery(tenantId );

		String employeeId = availabilityTimeFrame.getEmployee().getId();
		
		DateTime startDateTimeRange = startDateTime.minusDays(1);
		DateTime endDateTimeRange = startDateTime.plusDays(1);

        simpleQuery.addFilter("startDateTime <= '" + endDateTimeRange + "'");
        simpleQuery.addFilter("startDateTime >= '" + startDateTimeRange + "'");
        simpleQuery.addFilter("employee.primaryKey.id=" + "'" + employeeId + "'");
        
        // Only check for overlap of related availability types (avail/avail or pref/pref)...
        AvailabilityType type = availabilityTimeFrame.getAvailabilityType();     
        if (type.equals(AvailabilityType.Avail) || type.equals(AvailabilityType.UnAvail)){
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.AvailPreference + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvailPreference + "'");
        } 
        if (type.equals(AvailabilityType.AvailPreference) || type.equals(AvailabilityType.UnAvailPreference)) {
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.Avail + "'");
        	simpleQuery.addFilter("availabilityType != '" + AvailabilityType.UnAvail + "'");
        }

    	simpleQuery.setTotalCount(true);
        ResultSet<CDAvailabilityTimeFrame> overLapResultSet = findCDAvailabilityTimeFrames(simpleQuery);
        
        // remove availabilityTimeFrame from the result set so we'll only check for overlap with others
        Collection<CDAvailabilityTimeFrame> overLapCollection = overLapResultSet.getResult();
        if (overLapCollection.contains(availabilityTimeFrame)) {
        	overLapCollection.remove(availabilityTimeFrame);
        	overLapResultSet.setResult(overLapCollection);
        }
        
        if ((overLapResultSet.getResult() != null) && (overLapResultSet.getResult().size() > 0)) {
        	if (TimeUtil.checkCDCollectionForTimeOverLap(startDateTime, duration, overLapResultSet.getResult())) {
        		throw new ValidationException("This availability timeframe overlaps an existing timeframe.");
        	}
		}
	}  	

}
