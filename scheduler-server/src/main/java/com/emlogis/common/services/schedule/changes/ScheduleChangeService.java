package com.emlogis.common.services.schedule.changes;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.services.BaseService;
import com.emlogis.common.services.schedule.ShiftService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.schedule.ShiftChangeType;
import com.emlogis.model.schedule.changes.*;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.resources.util.ResultSet;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.rest.resources.util.SimpleQueryHelper;

import org.apache.commons.lang3.StringUtils;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.Collection;

import static com.emlogis.common.Constants.EMLOGIS_PERSISTENCE_UNIT_NAME;
import static com.emlogis.model.schedule.changes.ChangeCategory.ShiftChange;
import static com.emlogis.model.schedule.changes.ChangeType.SHIFTEDIT;

/**
 * Created by user on 24.09.15.
 */
@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ScheduleChangeService  extends BaseService {
	
    private static final String defaultTrackingStatuses = ScheduleStatus.Production.getValue() + "," 
            + ScheduleStatus.Posted.getValue();

    @PersistenceContext(unitName = EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;
    
    @EJB
    private ShiftService shiftService;

    public ResultSet<BaseScheduleChange> findChangeRecords(SimpleQuery simpleQuery) {
        simpleQuery.setEntityClass(BaseScheduleChange.class);
        SimpleQueryHelper sqh = new SimpleQueryHelper();
        return sqh.executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }


    public void trackShiftEditChange(
            Shift previousShift,
            Shift newShift,
            Schedule schedule,
	        String chgRequestId, 	// optional
	        UserAccount managerAccount, 	// optional
	        String reason			// optional
    ) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {
                ShiftEditChange change = createChange(schedule, ShiftEditChange.class, ShiftChange, SHIFTEDIT, 
                        chgRequestId, managerAccount, reason, true);
                change.setEmployeeAId(newShift.getEmployeeId());
                change.setEmployeeAName(newShift.getEmployeeName());
                change.setShiftId(previousShift.getId());
                change.setPreviousShiftCopy(EmlogisUtils.toJsonString(this.toDto(previousShift, ShiftTrackingDto.class)));
                change.setNewShiftCopy(EmlogisUtils.toJsonString(this.toDto(newShift, ShiftTrackingDto.class)));
                entityManager.merge(change);
                
            	setShiftChangeInfo(newShift, ShiftChangeType.EDIT, chgRequestId, managerAccount, 
                        newShift.getEmployeeName(), "Shift Modified");                
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }

    public void trackShiftWipChange(
            String previousEmployeeId,
            String previousEmployeeName,
            Shift shift,
            Schedule schedule,	        
            String chgRequestId, 	// optional
	        UserAccount managerAccount, 	// optional
	        String reason			// optional
    ) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {
                ShiftWipChange change = createChange(schedule, ShiftWipChange.class,
                        ChangeCategory.AssignmentChange, ChangeType.WIP, chgRequestId, managerAccount, reason, true);
                change.setShiftId(shift.getId());
                change.setEmployeeAId(previousEmployeeId);
                change.setEmployeeAName(previousEmployeeName);
                change.setEmployeeBId(shift.getEmployeeId());
                change.setEmployeeBName(shift.getEmployeeName());
                change.setShiftCopy(EmlogisUtils.toJsonString(this.toDto(shift, ShiftTrackingDto.class)));
            	String chgInfo = "Work In Place of '" + previousEmployeeName + "'";
                entityManager.merge(change);

                setShiftChangeInfo(shift, ShiftChangeType.WIP, chgRequestId, managerAccount, shift.getEmployeeName(), chgInfo);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }


	public void trackShiftSwapChange(
	        Shift shiftA,
	        Shift shiftB,
	        Schedule schedule,
	        String chgRequestId, 	   // optional
	        UserAccount managerAccount,// optional
	        String reason			   // optional
	) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {
                ShiftSwapChange change = createChange(schedule, ShiftSwapChange.class,
                        ChangeCategory.AssignmentChange, ChangeType.SWAP, chgRequestId, managerAccount, reason, true);
                change.setEmployeeAId(shiftA.getEmployeeId());
                change.setEmployeeAName(shiftA.getEmployeeName());
                change.setEmployeeAnewShiftCopy(EmlogisUtils.toJsonString(this.toDto(shiftA, ShiftTrackingDto.class)));
                change.setEmployeeAnewShiftId(shiftA.getId());              
                change.setEmployeeBId(shiftB.getEmployeeId());
                change.setEmployeeBName(shiftB.getEmployeeName());
                change.setEmployeeAnewShiftCopy(EmlogisUtils.toJsonString(this.toDto(shiftB, ShiftTrackingDto.class)));
                change.setEmployeeBnewShiftId(shiftB.getId()); 
                entityManager.merge(change);
                
                String chgAInfo = "Swap Shift with '" + shiftB.getEmployeeName() + "'";
            	setShiftChangeInfo(shiftA, ShiftChangeType.SWAP, chgRequestId, managerAccount, shiftA.getEmployeeName(), 
                        chgAInfo);
                String chgBInfo = "Swap Shift with '" + shiftA.getEmployeeName() + "'";
            	setShiftChangeInfo(shiftB, ShiftChangeType.SWAP, chgRequestId, managerAccount, shiftB.getEmployeeName(), 
                        chgBInfo);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }

    public void trackShiftDropChange(
            Shift shiftBefore,
            Shift shiftAfter,
            Schedule schedule,
            String chgRequestId, 	// optional
            UserAccount managerAccount, 	// optional
            String reason			// optional

    ) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {
                ShiftDropChange change = createChange(schedule, ShiftDropChange.class,
                        ChangeCategory.AssignmentChange, ChangeType.SHIFTDROP, chgRequestId, managerAccount, reason,
                        true);
                change.setDroppedShiftId(shiftAfter.getId());
                change.setEmployeeAId(shiftBefore.getEmployeeId());
                change.setEmployeeAName(shiftBefore.getEmployeeName());
                change.setDroppedShiftCopy(EmlogisUtils.toJsonString(this.toDto(shiftBefore, ShiftTrackingDto.class)));
                entityManager.merge(change);
                
            	setShiftChangeInfo(shiftAfter, ShiftChangeType.DROP, chgRequestId, managerAccount,
                        shiftBefore.getEmployeeName(), "Shift Drop");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }

    public void trackShiftAddChange(
            Shift shift,
            Schedule schedule,
            String chgRequestId, 	// optional
            UserAccount managerAccount,// optional
            String reason			// optional
    ) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {
            	ShiftAddChange change = createChange(schedule, ShiftAddChange.class,
                        ChangeCategory.ShiftChange, ChangeType.SHIFTADD, chgRequestId, managerAccount, reason, true);
            	
                change.setShiftId(shift.getId());
                change.setShiftCopy(EmlogisUtils.toJsonString(this.toDto(shift, ShiftTrackingDto.class)));
                entityManager.persist(change);
                
            	setShiftChangeInfo(shift, ShiftChangeType.CREATE, chgRequestId, managerAccount, shift.getEmployeeName(), 
                        "Shift Create");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }

    public void trackShiftDeleteChange(
            Shift shift,
            Schedule schedule,
            String chgRequestId, 	// optional
            UserAccount managerAccount, 	// optional
            String reason			// optional
    ) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {
            	ShiftDeleteChange change = createChange(schedule, ShiftDeleteChange.class,
                        ChangeCategory.ShiftChange, ChangeType.SHIFTDELETE, chgRequestId, managerAccount, reason, true);

                change.setEmployeeAId(shift.getEmployeeId());
                change.setEmployeeAName(shift.getEmployeeName());
                change.setShiftId(shift.getId());
                change.setPreviousShiftCopy(EmlogisUtils.toJsonString(this.toDto(shift, ShiftTrackingDto.class)));

                entityManager.persist(change);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }

    public void trackShiftAssignmentChange(
            Shift shift,
            Schedule schedule,
            String chgRequestId, 	// optional
            UserAccount managerAccount,// optional
            String reason			// optional
    ) {
        if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
            try {        	
            	ShiftAssignChange change = createChange(schedule, ShiftAssignChange.class, 
                        ChangeCategory.AssignmentChange, ChangeType.SHIFTASSIGN, chgRequestId, managerAccount, reason, 
                        true);
            	
                change.setEmployeeAId(shift.getEmployeeId());
                change.setEmployeeAName(shift.getEmployeeName());
                change.setShiftId(shift.getId());
                change.setShiftCopy(EmlogisUtils.toJsonString(this.toDto(shift, ShiftTrackingDto.class)));
                entityManager.persist(change);
                
            	setShiftChangeInfo(shift, ShiftChangeType.ASSIGN, chgRequestId, managerAccount, shift.getEmployeeName(), 
                        "Shift Assign");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }
        }
    }

    public <T extends BaseScheduleChange> T create(
            Class<T> changeRecordClass,
            ChangePrimaryKey primaryKey,
            boolean doPersist) throws IllegalAccessException, InstantiationException {
        try {
            // create the change record instance according to the specified class
            T result = changeRecordClass.newInstance();
            result.setPrimaryKey(primaryKey);

            if (doPersist) {
                entityManager.persist(result);
            }
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    public void insert(Collection<BaseScheduleChange> list) {
        // persist all records
        for (BaseScheduleChange baseScheduleChange : list) {
            entityManager.persist(baseScheduleChange);
        }
    }

    public <T extends BaseScheduleChange> T createChange(
            Schedule schedule,
            Class<T> changeRecordClass,
            ChangeCategory changeCategory,
            ChangeType changeType,
	        String chgRequestId, 		// optional
	        UserAccount managerAccount, 	// optional
	        String reason,				// optional
            boolean doPersist) {
        try {
            if (isTrackingEnabled() && isTrackingForStatusEnabled(schedule)) {
                ChangePrimaryKey changeSchedulePrimaryKey = new ChangePrimaryKey(schedule.getPrimaryKey().getTenantId());
                T scheduleChange = null;
                try {
                    scheduleChange = create(changeRecordClass, changeSchedulePrimaryKey, doPersist);
                    scheduleChange.setScheduleId(schedule.getId());
                    scheduleChange.setChangeDate(System.currentTimeMillis());
                    scheduleChange.setScheduleName(schedule.getName());
                    scheduleChange.setScheduleStartDate(schedule.getStartDate());
                    scheduleChange.setScheduleEndDate(schedule.getEndDate());
                    scheduleChange.setCategory(changeCategory);
                    if (changeType.isInChangeCategory(changeCategory)) {
                        scheduleChange.setType(changeType);
                    }
                    scheduleChange.setChangeRequestId(chgRequestId);
                    if (managerAccount != null) {
                    	scheduleChange.setChangeEmployeeId(managerAccount.getId());
                    	scheduleChange.setChangeEmployeeName(managerAccount.getName());
                    }
                    scheduleChange.setReason(reason);
                    entityManager.merge(scheduleChange);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
                return scheduleChange;
            } else {
                return null;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    private boolean isTrackingEnabled() {
        return Boolean.parseBoolean(System.getProperty(Constants.TRACKING_ENABLED));
    }
    
    private boolean isTrackingForStatusEnabled(com.emlogis.model.schedule.Schedule schedule) {
        if (schedule.getStatus() != null) {
            String trackingStatuses = System.getProperty(Constants.TRACKING_STATUSES);
            trackingStatuses = StringUtils.isNotBlank(trackingStatuses) ? trackingStatuses : defaultTrackingStatuses;
            String[] statuses = trackingStatuses.split(",");
            for (String status : statuses) {
                if (schedule.getStatus().getValue().equalsIgnoreCase(status)) {
                    return true;
                }
            }
        }
        return false;
    }

	private void setShiftChangeInfo(
            Shift shift,
            ShiftChangeType chgType,
            String chgRequestId,
            UserAccount managerAccount,
            String chgEmployeeName,
            String chgInfo) {
        try {
        	// as shift passed as parameter can be a detached one, get a fresh copy from Db and update it.
        	Shift shiftToUpdate = shiftService.getShift(shift.getPrimaryKey()); 
        	shiftToUpdate.setChgType(chgType);
        	shiftToUpdate.setChgRequestId(chgRequestId);
        	shiftToUpdate.setChgManagerName(managerAccount != null ? managerAccount.getName() : null);
        	shiftToUpdate.setChgEmployeeName(chgEmployeeName);
        	shiftToUpdate.setChgInfo(chgInfo);
        	shiftToUpdate.setChanged(System.currentTimeMillis());
        	shiftService.update(shiftToUpdate);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
	}

    public void delete(BaseScheduleChange record) {
        entityManager.remove(record);
    }

    public void deleteBySchedule(PrimaryKey schedulePrimaryKey) {
        SimpleQuery simpleQuery = new SimpleQuery(schedulePrimaryKey.getTenantId());
        simpleQuery.setFilter("scheduleId = '" + schedulePrimaryKey.getId() + "'");
        ResultSet<BaseScheduleChange> changeResultSet = findChangeRecords(simpleQuery);
        for (BaseScheduleChange change : changeResultSet.getResult()) {
            entityManager.remove(change);
        }
    }

}
