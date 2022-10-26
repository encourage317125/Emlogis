package com.emlogis.common.services.employee;

import com.emlogis.common.Constants;
import com.emlogis.common.TimeUtil;
import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.CacheConstants;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.AvailabilityTimeFrame.AvailabilityType;
import com.emlogis.model.employee.CIAvailabilityTimeFrame;
import com.emlogis.model.employee.Employee;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;
import com.emlogis.server.services.cache.BasicCacheService;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
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
public class CIAvailabilityTimeFrameService {
	
	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    @EJB
    private BasicCacheService cacheService;

	/**
	 * findCIAvailabilityTimeFrames() find a list of CIAvailabilityTimeFrames matching criteria;
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public ResultSet<CIAvailabilityTimeFrame> findCIAvailabilityTimeFrames(SimpleQuery sq)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		sq.setEntityClass(CIAvailabilityTimeFrame.class);
		SimpleQueryHelper sqh = new SimpleQueryHelper();
		return sqh.executeSimpleQueryWithPaging(entityManager, sq);
	}	
	
	/**
	 * Get availabilityTimeFrame 
	 * @param primaryKey
	 * @return
	 */
	public CIAvailabilityTimeFrame getCIAvailabilityTimeFrame(PrimaryKey primaryKey) {
		return entityManager.find(CIAvailabilityTimeFrame.class, primaryKey);
	}

	/**
	 * Create CIAvailabilityTimeFrameService
	 * @param primaryKey
	 * @param employee
	 * @param absenceType
	 * @param reason
	 * @param startTime
	 * @param duration
	 * @param availabilityType
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
    public CIAvailabilityTimeFrame createCIAvailabilityTimeFrame(PrimaryKey primaryKey, Employee employee,
            AbsenceType absenceType, String reason, LocalTime startTime, Minutes duration,
            AvailabilityType availabilityType, DayOfWeek dayOfTheWeek, DateTime startDateTime, DateTime endDateTime)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            ValidationException {
        CIAvailabilityTimeFrame availabilityTimeFrame = new CIAvailabilityTimeFrame(primaryKey, employee, absenceType,
                reason, startTime, duration, availabilityType, dayOfTheWeek, startDateTime, endDateTime);

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
    public CIAvailabilityTimeFrame update(CIAvailabilityTimeFrame availabilityTimeFrame) 
    		throws InstantiationException, IllegalAccessException, InvocationTargetException, ValidationException {
        availabilityTimeFrame.touch();
		validateTimeFrame(availabilityTimeFrame);
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, availabilityTimeFrame.getTenantId(),
                availabilityTimeFrame.getEmployeeId());
        return entityManager.merge(availabilityTimeFrame);
    }   
    
    /**
     * Delete availabilityTimeFrame
     * @param availabilityTimeFrame
     */
    public void delete(CIAvailabilityTimeFrame availabilityTimeFrame) {
    	availabilityTimeFrame.getEmployee().removeAvailabilityTimeFrame(availabilityTimeFrame);
    	entityManager.persist(availabilityTimeFrame.getEmployee());
        entityManager.remove(availabilityTimeFrame);
        cacheService.clearEntry(CacheConstants.EMP_AVAILABILITY_CACHE, availabilityTimeFrame.getTenantId(),
                availabilityTimeFrame.getEmployeeId());
    }

    /**
	 * Checks that this the given calendar independent time frame doesn't span
	 * across midnight and doesn't overlap any other calendar independent
	 * time frame for the associated employee.
	 * 
     * @param availabilityTimeFrame
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ValidationException
     */
	private void validateTimeFrame(CIAvailabilityTimeFrame availabilityTimeFrame) throws InstantiationException,
            IllegalAccessException, InvocationTargetException, ValidationException {
		if (AvailabilityType.Avail.equals(availabilityTimeFrame.getAvailabilityType())) {
			throw new ValidationException("A calendar independent availability timeframe can only represent unavailability");			
		}
		
		// We are actually using the next day here to account for midnight
		DateTime endDateTime =  availabilityTimeFrame.getEndDateTime();
		
		// Check that this date won't span multiple days
		DateTime startDateTime = availabilityTimeFrame.getStartDateTime();
		LocalTime startTime = availabilityTimeFrame.getStartTime();
		Minutes duration = availabilityTimeFrame.getDurationInMinutes();
		if (TimeUtil.spansMultipleDays(startDateTime, startTime, duration)) {
			throw new ValidationException("An availability timeframe cannot span multiple days");
		}
		
		// Check this timeframe won't overlap with any existing CI timeframes for this employee
		
		String tenantId = availabilityTimeFrame.getTenantId();
		SimpleQuery simpleQuery = new SimpleQuery(tenantId );

		String employeeId = availabilityTimeFrame.getEmployee().getId();
		DayOfWeek dayOfTheWeek = availabilityTimeFrame.getDayOfTheWeek();
		String filter = " startDateTime < '"  + endDateTime + "' AND endDateTime > '" + startDateTime + "'"
                + " AND employee.primaryKey.id = " + "'" + employeeId  + "' "
                + " AND dayOfTheWeek = com.emlogis.engine.domain.DayOfWeek." + dayOfTheWeek.name() + " ";
		simpleQuery.setFilter(filter).setTotalCount(true);

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
	
	    ResultSet<CIAvailabilityTimeFrame> overLapResultSet = findCIAvailabilityTimeFrames(simpleQuery);
	
        // remove availabilityTimeFrame from the result set so we'll only check for overlap with others
        Collection<CIAvailabilityTimeFrame> overLapCollection = overLapResultSet.getResult();
        if (overLapCollection.contains(availabilityTimeFrame)) {
        	overLapCollection.remove(availabilityTimeFrame);
        	overLapResultSet.setResult(overLapCollection);
        }

        if ((overLapResultSet != null) && (overLapResultSet.getResult() != null) && (overLapResultSet.getResult().size() > 0)) {
	    	if (TimeUtil.checkCICollectionForTimeOverLap(startTime, duration, overLapResultSet.getResult())) {
	    		throw new ValidationException("This availability timeframe overlaps an existing timeframe.");
	    	}
		}
	}  	

}
