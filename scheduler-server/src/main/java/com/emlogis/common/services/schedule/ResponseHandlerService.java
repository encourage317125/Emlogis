package com.emlogis.common.services.schedule;

import com.emlogis.common.Constants;
import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.common.services.schedule.changes.ScheduleChangeService;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.UserAccountService;
import com.emlogis.engine.domain.communication.AssignmentResultDto;
import com.emlogis.engine.domain.communication.QualificationResultDto;
import com.emlogis.engine.domain.communication.ScheduleResultDto;
import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.communication.ShiftSwapEligibilityResultDto;
import com.emlogis.engine.domain.communication.constraints.ScoreLevelResultDto;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.Skill;
import com.emlogis.model.schedule.*;
import com.emlogis.model.schedule.Schedule;
import com.emlogis.model.schedule.dto.QualificationRequestTrackerDto;
import com.emlogis.model.schedule.dto.ScheduleDto;
import com.emlogis.model.shiftpattern.ShiftLength;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.rest.security.SessionService;
import com.emlogis.scheduler.engine.communication.request.RequestType;
import com.emlogis.scheduler.engine.communication.response.EngineResponse;
import com.emlogis.scheduler.engine.communication.response.EngineResponseData;
import com.emlogis.scheduler.engine.communication.response.EngineResponseType;
import com.emlogis.server.services.eventservice.ASEventService;
import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;
import com.hazelcast.core.IMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import reactor.event.Event;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.*;

@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ResponseHandlerService {

	private final static Logger logger = Logger.getLogger(ResponseHandlerService.class);

	@PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

	@EJB
	private SessionService sessionService;

	@EJB
	private ScheduleService scheduleService;

	@EJB
	private SkillService skillService;

	@EJB
	private SiteService siteService;

	@EJB
	private TeamService teamService;

	@EJB
	private ShiftService shiftService;

	@EJB
	private HazelcastClientService hazelcastClientService;

	@EJB
	private UserAccountService userAccountService;

	@Inject
	private ASEventService eventService;

	@EJB
	private ScheduleChangeService scheduleChangeService;

	public static class ResultMonitor {
		private boolean wasSignaled = false;
		private ScheduleResultDto result = null;

		public synchronized boolean getWasSignaled() {
			return wasSignaled;
		}

		public synchronized void setWasSignaled(boolean wasSignaled) {
			this.wasSignaled = wasSignaled;
		}

		public synchronized ScheduleResultDto getResult() {
			return result;
		}

		public synchronized void setResult(ScheduleResultDto result) {
			this.result = result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "ResultMonitor [wasSignaled=" + wasSignaled + ", result=" + result + "]";
		} 

	}

	private static Map<String, ResultMonitor> resultMonitorMap = new HashMap<>();

	public void putResultMonitor(String requestId, ResultMonitor resultMonitor) {
		resultMonitorMap.put(requestId, resultMonitor);
	}

	public ResultMonitor getResultMonitor(String requestId){
		return resultMonitorMap.get(requestId);
	}

	public ASEventService getEventService() {
		return eventService;
	}

	public void setEventService(ASEventService eventService) {
		this.eventService = eventService;
	}

	public void handleResponse(EngineResponse engineResponse, final IMap<String, String> responseDataMap) {
		long responseReceived = System.currentTimeMillis();

		logger.info(String.format("EngineResponse take: scheduleId=%s(%s) requestId=%s requestType=%s reponseType=%s EngineResponseType=%s from:%s(%s)",
				engineResponse.getScheduleId(), engineResponse.getScheduleName(), engineResponse.getRequestId(),
				engineResponse.getRequestType(),engineResponse.getClass().getSimpleName(),
                engineResponse.getEngineResponseType(), engineResponse.getEngineId(), engineResponse.getEngineLabel()));

		String requestId = engineResponse.getRequestId();
		RequestType requestType = engineResponse.getRequestType();


		if (RequestType.Assignment.equals(requestType)) {
			String scheduleId = engineResponse.getScheduleId();
			String tenantId = engineResponse.getTenantId();
			PrimaryKey schedulePrimaryKey = new PrimaryKey();
			schedulePrimaryKey.setId(scheduleId);
			schedulePrimaryKey.setTenantId(tenantId);
			Schedule schedule = scheduleService.getSchedule(schedulePrimaryKey);
			if (schedule != null) {
				TaskState oldState = schedule.getState();

				if (EngineResponseType.Completion.equals(engineResponse.getEngineResponseType())
						|| EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
					long responseProcessingStartTime = System.currentTimeMillis();

					if (EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
						hazelcastClientService.removeRequestIdFromAbortMap(requestId);
					}
					schedule.setState(TaskState.Complete);
					schedule.setResponseReceivedDate(responseReceived);
					schedule.setCompletion(engineResponse.getCompletion());
					schedule.setCompletionInfo(engineResponse.getCompletionInfo());

					ScheduleReport oldScheduleReport = null;
					try {
						// get response data if available
						String responseDataJson = responseDataMap.get(requestId);
						EngineResponseData responseData =
								EmlogisUtils.fromJsonString(responseDataJson,EngineResponseData.class);
						// always release the entry from response map, regardless of status
						responseDataMap.delete(requestId);

						// get manager account for associating all changes to  account 
						UserAccount  managerAccount = userAccountService.getUserAccount(new PrimaryKey(tenantId,
                                engineResponse.getAccountId()));

						AssignmentResultDto assignmentResultData =
								EmlogisUtils.fromJsonString(responseData.getResponseData(), AssignmentResultDto.class);

						String awsUse = System.getProperty(Constants.SCHEDULE_REPORT_AWS_USE);
						boolean storeScheduleReportInDb = !"true".equalsIgnoreCase(awsUse);

						if (assignmentResultData != null) {
							ScheduleReport scheduleReport = null;
							if (storeScheduleReportInDb) {
								oldScheduleReport = schedule.getScheduleReport();
								scheduleReport = new ScheduleReport(new PrimaryKey(tenantId));
								scheduleReport.setCompletionReport(
										EmlogisUtils.toJsonString(assignmentResultData.getConstraintMatchTotals()));
								scheduleService.createScheduleReport(scheduleReport);

								schedule.setScheduleReport(scheduleReport);
							}

							// TODO move this piece of code into a separate method
							// Aggregate the hardscores to build the aggregated hardscore
							List<ScoreLevelResultDto> hardScores = assignmentResultData.getHardScores();
							if (hardScores != null) {
								int hardScore = 0;
								for (ScoreLevelResultDto slr : hardScores) {
									hardScore += slr.getTotalScoreValue();
								}
								schedule.setHardScore(hardScore);
								if (scheduleReport != null) {
									scheduleReport.setHardScoreDetails(EmlogisUtils.toJsonString(hardScores));
								}
							}
							List<ScoreLevelResultDto> softScores = assignmentResultData.getSoftScores();
							if (softScores != null) {
								int softScore = 0;
								for (ScoreLevelResultDto slr : softScores) {
									softScore += slr.getTotalScoreValue();
								}
								schedule.setSoftScore(softScore);
								if (scheduleReport != null) {
									scheduleReport.setSoftScoreDetails(EmlogisUtils.toJsonString(softScores));
								}
							}

							int assignmentCount = storeAssignments(schedule, assignmentResultData.getShiftAssignments(),
									managerAccount);
							schedule.setReturnedAssignedShifts(assignmentCount);
							schedule.setReturnedOpenShifts(scheduleService.unassignedShiftCount(schedule));
						}
						hazelcastClientService.removeRequestAndResponseData(schedule.getRequestId());
						notifyProgress(schedule, 100, schedule.getHardScore(), schedule.getMediumScore(),
								schedule.getSoftScore(), "Schedule Generation Complete"); // TODO String to be I18n
					} finally {
						schedule.setExecutionEndDate(System.currentTimeMillis());
						scheduleService.update(schedule);
						if (oldScheduleReport != null) {
							scheduleService.deleteScheduleReport(oldScheduleReport);
						}
						entityManager.flush();
						schedule.setResponseProcessingDuration(System.currentTimeMillis() - responseProcessingStartTime);
						scheduleService.update(schedule);
						getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);
					}
				} else if (EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
					if (TaskState.Queued.equals(schedule.getState())) {
						schedule.setExecutionAckDate(System.currentTimeMillis());
						schedule.setState(TaskState.Running);
						schedule.setEngineId(engineResponse.getEngineId());
						schedule.setEngineLabel(engineResponse.getEngineLabel());
						scheduleService.update(schedule);
						getEventService().sendEntityUpdateEvent(schedule, ScheduleDto.class);
					} else {
						// TODO what to do in this situation?
					}
				}
				logger.info(String.format("Schedule [scheduleId=%s] old state was %s; new state is %s ",
						engineResponse.getScheduleId(), oldState, schedule.getState()));
			}
		} else if (RequestType.Qualification.equals(requestType) ) {
			QualificationRequestTracker requestTracker = hazelcastClientService.getQualificationRequestTracker(requestId);
			if (requestTracker != null) {
				TaskState oldState = requestTracker.getState();

				if (EngineResponseType.Completion.equals(engineResponse.getEngineResponseType())
						|| EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
					long responseProcessingStartTime = System.currentTimeMillis();

					if (EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
						hazelcastClientService.removeRequestIdFromAbortMap(requestId);
					}
					requestTracker.setState(TaskState.Complete);
					requestTracker.setResponseReceivedDate(responseReceived);
					requestTracker.setCompletion(engineResponse.getCompletion());
					requestTracker.setCompletionInfo(engineResponse.getCompletionInfo());

					try {
						// get response data if available
						String responseDataJson = responseDataMap.get(requestId);
						EngineResponseData responseData =
								EmlogisUtils.fromJsonString(responseDataJson,EngineResponseData.class);
						// always release the entry from response map, regardless of status
						responseDataMap.delete(requestId);

						ResultMonitor resultMonitor = resultMonitorMap.get(requestId); // in case of synchronous processing
						QualificationResultDto qualificationResultData =
								EmlogisUtils.fromJsonString(responseData.getResponseData(), QualificationResultDto.class);
						if (qualificationResultData != null) {

							requestTracker.setQualificationShifts(qualificationResultData.getQualifyingShifts());

							if (resultMonitor != null) {
								synchronized (resultMonitor) {
									resultMonitor.setResult(qualificationResultData);
								}
							} else {
								// TODO Will this ever happen?
							}
						}
						hazelcastClientService.removeRequestAndResponseData(requestId);
						if (resultMonitor != null) {
							synchronized (resultMonitor) {
								resultMonitor.setWasSignaled(true);
								resultMonitor.notify();
							}
						}

						String progressInfo;
						if (requestType.equals(RequestType.Qualification)){
							progressInfo = "Qualification Determination Complete"; // TODO String to be I18n							
						} else {
							progressInfo = "Elligibility Determination Complete"; // TODO String to be I18n														
						}
						notifyProgress(requestTracker, 100, requestTracker.getHardScore(), requestTracker.getSoftScore(),
								progressInfo); // TODO String to be I18n														
					} finally {
						requestTracker.setExecutionEndDate(System.currentTimeMillis());
						requestTracker.setResponseProcessingDuration(System.currentTimeMillis() - responseProcessingStartTime);
						hazelcastClientService.putQualificationRequestTracker(requestId, requestTracker);
						getEventService().sendEntityUpdateEvent(requestTracker, QualificationRequestTrackerDto.class);
					}
				} else if (EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
					if (TaskState.Queued.equals(requestTracker.getState())) {
						requestTracker.setExecutionAckDate(System.currentTimeMillis());
						requestTracker.setState(TaskState.Running);
						requestTracker.setEngineId(engineResponse.getEngineId());
						requestTracker.setEngineLabel(engineResponse.getEngineLabel());
						hazelcastClientService.putQualificationRequestTracker(requestId, requestTracker);
						getEventService().sendEntityUpdateEvent(requestTracker, QualificationRequestTrackerDto.class);
					} else {
						// TODO what to do in this situation?
					}
				}
				logger.info(String.format("QualificationRequestTrackerDto [requestId=%s] old state was %s; new state is %s ",
						requestTracker.getRequestId(), oldState, requestTracker.getState()));
			}
		} else if (RequestType.OpenShiftEligibility.equals(requestType)) {
			// TODO: Switch from QualificationRequestTracker to OpenShifteligibilityRequestTracker
			QualificationRequestTracker requestTracker = hazelcastClientService.getQualificationRequestTracker(requestId);
			if (requestTracker != null) {
				TaskState oldState = requestTracker.getState();

				if (EngineResponseType.Completion.equals(engineResponse.getEngineResponseType())
						|| EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
					long responseProcessingStartTime = System.currentTimeMillis();

					if (EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
						hazelcastClientService.removeRequestIdFromAbortMap(requestId);
					}
					requestTracker.setState(TaskState.Complete);
					requestTracker.setResponseReceivedDate(responseReceived);
					requestTracker.setCompletion(engineResponse.getCompletion());
					requestTracker.setCompletionInfo(engineResponse.getCompletionInfo());

					try {
						// get response data if available
						String responseDataJson = responseDataMap.get(requestId);
						EngineResponseData responseData =
								EmlogisUtils.fromJsonString(responseDataJson,EngineResponseData.class);
						// always release the entry from response map, regardless of status
						responseDataMap.delete(requestId);

						ResultMonitor resultMonitor = resultMonitorMap.get(requestId); // in case of synchronous processing
						QualificationResultDto qualificationResultData =
								EmlogisUtils.fromJsonString(responseData.getResponseData(), QualificationResultDto.class);
						if (qualificationResultData != null) {

							requestTracker.setQualificationShifts(qualificationResultData.getQualifyingShifts());

							if (resultMonitor != null) {
								synchronized (resultMonitor) {
									resultMonitor.setResult(qualificationResultData);
								}
							} else {
								// TODO Will this ever happen?
							}
						}
						hazelcastClientService.removeRequestAndResponseData(requestId);
						if (resultMonitor != null) {
							synchronized (resultMonitor) {
								resultMonitor.setWasSignaled(true);
								resultMonitor.notify();
							}
						}

						String progressInfo;
						if (requestType.equals(RequestType.Qualification)){
							progressInfo = "Qualification Determination Complete"; // TODO String to be I18n							
						} else {
							progressInfo = "Elligibility Determination Complete"; // TODO String to be I18n														
						}
						notifyProgress(requestTracker, 100, requestTracker.getHardScore(), requestTracker.getSoftScore(),
								progressInfo); // TODO String to be I18n														
					} finally {
						requestTracker.setExecutionEndDate(System.currentTimeMillis());
						requestTracker.setResponseProcessingDuration(System.currentTimeMillis() - responseProcessingStartTime);
						hazelcastClientService.putQualificationRequestTracker(requestId, requestTracker);
						getEventService().sendEntityUpdateEvent(requestTracker, QualificationRequestTrackerDto.class);
					}
				} else if (EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
					if (TaskState.Queued.equals(requestTracker.getState())) {
						requestTracker.setExecutionAckDate(System.currentTimeMillis());
						requestTracker.setState(TaskState.Running);
						requestTracker.setEngineId(engineResponse.getEngineId());
						requestTracker.setEngineLabel(engineResponse.getEngineLabel());
						hazelcastClientService.putQualificationRequestTracker(requestId, requestTracker);
						getEventService().sendEntityUpdateEvent(requestTracker, QualificationRequestTrackerDto.class);
					} else {
						// TODO what to do in this situation?
					}
				}
				logger.info(String.format("QualificationRequestTrackerDto [requestId=%s] old state was %s; new state is %s ",
						requestTracker.getRequestId(), oldState, requestTracker.getState()));
			}
		} else if (RequestType.ShiftSwapEligibility.equals(requestType)) {
			ShiftSwapEligibilityRequestTracker requestTracker = hazelcastClientService.getShiftSwapEligibilityRequestTracker(requestId);
			if (requestTracker != null) {
				TaskState oldState = requestTracker.getState();

				if (EngineResponseType.Completion.equals(engineResponse.getEngineResponseType())
						|| EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
					long responseProcessingStartTime = System.currentTimeMillis();

					if (EngineResponseType.Abort.equals(engineResponse.getEngineResponseType())) {
						hazelcastClientService.removeRequestIdFromAbortMap(requestId);
					}
					requestTracker.setState(TaskState.Complete);
					requestTracker.setResponseReceivedDate(responseReceived);
					requestTracker.setCompletion(engineResponse.getCompletion());
					requestTracker.setCompletionInfo(engineResponse.getCompletionInfo());

					try {
						// get response data if available
						String responseDataJson = responseDataMap.get(requestId);
						EngineResponseData responseData =
								EmlogisUtils.fromJsonString(responseDataJson,EngineResponseData.class);
						// always release the entry from response map, regardless of status
						responseDataMap.delete(requestId);

						ResultMonitor resultMonitor = resultMonitorMap.get(requestId); // in case of synchronous processing
						ShiftSwapEligibilityResultDto shiftSwapEligibilityResultData =
								EmlogisUtils.fromJsonString(responseData.getResponseData(), ShiftSwapEligibilityResultDto.class);
						if (shiftSwapEligibilityResultData != null) {

							requestTracker.setQualificationShifts(shiftSwapEligibilityResultData.getQualifyingShifts());

							if (resultMonitor != null) {
								synchronized (resultMonitor) {
									resultMonitor.setResult(shiftSwapEligibilityResultData);
								}
							} else {
								// TODO Will this ever happen?
							}
						}
						hazelcastClientService.removeRequestAndResponseData(requestId);
						if (resultMonitor != null) {
							synchronized (resultMonitor) {
								resultMonitor.setWasSignaled(true);
								resultMonitor.notify();
							}
						}

						String progressInfo;
						if (requestType.equals(RequestType.Qualification)){
							progressInfo = "Qualification Determination Complete"; // TODO String to be I18n							
						} else {
							progressInfo = "Elligibility Determination Complete"; // TODO String to be I18n														
						}
						notifyProgress(requestTracker, 100, requestTracker.getHardScore(), requestTracker.getSoftScore(),
								progressInfo); // TODO String to be I18n														
					} finally {
						requestTracker.setExecutionEndDate(System.currentTimeMillis());
						requestTracker.setResponseProcessingDuration(System.currentTimeMillis() - responseProcessingStartTime);
						hazelcastClientService.putShiftSwapEligibilityRequestTracker(requestId, requestTracker);
						//  TODO Fix this!
						//                        getEventService().sendEntityUpdateEvent(requestTracker, ShiftSwapEligibilityRequestTrackerDto.class);
					}
				} else if (EngineResponseType.Acknowledge.equals(engineResponse.getEngineResponseType())) {
					if (TaskState.Queued.equals(requestTracker.getState())) {
						requestTracker.setExecutionAckDate(System.currentTimeMillis());
						requestTracker.setState(TaskState.Running);
						requestTracker.setEngineId(engineResponse.getEngineId());
						requestTracker.setEngineLabel(engineResponse.getEngineLabel());
						hazelcastClientService.putShiftSwapEligibilityRequestTracker(requestId, requestTracker);
						//  TODO Fix this!
						//						getEventService().sendEntityUpdateEvent(requestTracker, QualificationRequestTrackerDto.class);
					} else {
						// TODO what to do in this situation?
					}
				}
				logger.info(String.format("QualificationRequestTrackerDto [requestId=%s] old state was %s; new state is %s ",
						requestTracker.getRequestId(), oldState, requestTracker.getState()));
			}
		} else if (RequestType.Shutdown.equals(requestType)) {
			// TODO Special handling for shutdown?
		} else if (RequestType.Abort.equals(requestType)) {
			// TODO Special handling for abort?
		}
	}

	private int storeAssignments(Schedule schedule, List<ShiftAssignmentDto> shiftAssignments,
                                 UserAccount managerAccount) {
		int result = 0;

		Map<String, ShiftAssignmentDto> assignmentMap = new HashMap<>();
		for (ShiftAssignmentDto shiftAssignment : shiftAssignments) {
			assignmentMap.put(shiftAssignment.getShiftId(), shiftAssignment);
		}

		Collection<Shift> shifts = shiftService.getShifts(schedule.getTenantId(), assignmentMap.keySet());
		if (shifts != null) {
			for (Shift shift : shifts) {
				ShiftAssignmentDto shiftAssignment = assignmentMap.get(shift.getId());

				boolean modified = false;
				if (!StringUtils.equals(shiftAssignment.getEmployeeId(), shift.getEmployeeId())
                        && !StringUtils.equals(shiftAssignment.getEmployeeName(), shift.getEmployeeName())) {
					shift.makeShiftAssignment(shiftAssignment.getEmployeeId(), shiftAssignment.getEmployeeName(),
                            AssignmentType.ENGINE);
					modified = true;
				}
				if (shiftAssignment.isExcess() != shift.isExcess()) {
					shift.setExcess(shiftAssignment.isExcess());
					modified = true;
				}
				if (shiftAssignment.isLocked() != shift.isLocked()) {
					shift.setLocked(shiftAssignment.isLocked());
					modified = true;
				}

				if (modified) {
					shift = shiftService.update(shift);
					scheduleChangeService.trackShiftAssignmentChange(shift, schedule, null, managerAccount,
                            "Assigned by engine");
				}

				assignmentMap.remove(shift.getId());

				if (shift.getEmployeeId() != null && !AssignmentType.MANUAL.equals(shift.getAssignmentType())
                        && StringUtils.equals(shift.getScheduleId(), schedule.getId())) {
					result++;
				}
			}
		}

		// check for shifts generated by 'forceCompletion'
		for (ShiftAssignmentDto shiftAssignmentDto : assignmentMap.values()) {
			if (StringUtils.isNotEmpty(shiftAssignmentDto.getShiftId())
					&& shiftAssignmentDto.getShiftId().startsWith("FC-")) {
				shiftAssignmentDto.setExcess(true);
				Shift shift = createShift(schedule, shiftAssignmentDto, managerAccount);
				if (shift.getEmployeeId() != null) {
					result++;
				}
			}
		}

		return result;
	}

	private Shift createShift(Schedule schedule, ShiftAssignmentDto shiftAssignmentDto, UserAccount managerAccount) {
		String tenantId = schedule.getTenantId();

		Shift shift = new Shift(new PrimaryKey(tenantId));
		shift.setScheduleId(schedule.getId());

		shift.setEndDateTime(shiftAssignmentDto.getShiftEndDateTime().getMillis());
		shift.setStartDateTime(shiftAssignmentDto.getShiftStartDateTime().getMillis());

		shift.setShiftLength((int) (shift.getEndDateTime() - shift.getStartDateTime()) / 1000 / 60);

		Skill skill = skillService.getSkill(new PrimaryKey(tenantId, shiftAssignmentDto.getShiftSkillId()));

		shift.setSkillId(skill.getId());
		shift.setSkillAbbrev(skill.getAbbreviation());
		shift.setSkillName(skill.getName());

		Team team = teamService.getTeam(new PrimaryKey(tenantId, shiftAssignmentDto.getTeamId()));
		Site site = teamService.getSite(team);

		shift.setTeamId(team.getId());
		shift.setTeamName(team.getName());
		shift.setSiteName(site.getName());

		ShiftLength shiftLength = siteService.getShiftLengthByLength(site, shift.getShiftLength());
		shift.setShiftLengthId(shiftLength.getId());
		shift.setShiftLengthName(shiftLength.getName());
		//        shift.setPaidTime(shiftLength.getPaidTimeInMin());  to be updated ad paidtime is now in SHiftType

		shift.setExcess(shiftAssignmentDto.isExcess());
		shift.setLocked(shiftAssignmentDto.isLocked());
		shift.makeShiftAssignment(shiftAssignmentDto.getEmployeeId(), shiftAssignmentDto.getEmployeeName(),
                AssignmentType.ENGINE);

		shiftService.insert(shift);
		scheduleChangeService.trackShiftAddChange(shift, schedule, null, managerAccount, null);

		return shift;
	}

	private void notifyProgress(Schedule schedule, int progress, int hardScore, int mediumScore, int softScore,
			String progressInfo) {
		ASEventService eventService = getEventService();
		Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM_NOTIFICATION)
				.setTenantId(schedule.getTenantId())
				.setEntityClass("Schedule").setEventType("Progress").setEntityId(schedule.getId()).build();
		Map<String,Object> eventBody = new HashMap<>();
		eventBody.put("progress", progress);
		eventBody.put("hardScore", hardScore);
		eventBody.put("mediumScore", mediumScore);
		eventBody.put("softScore", softScore);
		eventBody.put("msg", progressInfo);
		try {
			eventService.sendEvent(EventScope.AppServer, key, Event.wrap(eventBody),"SchedulingService");
		} catch (Throwable t) {
			logger.error("notifyProgress Error",t);
		}
	}

	private void notifyProgress(QualificationRequestTracker requestTracker, int progress, int hardScore, int softScore,
			String progressInfo) {
		ASEventService eventService = getEventService();
		Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM_NOTIFICATION)
				.setTenantId(requestTracker.getTenantId())
				.setEntityClass("QualificationRequestTrackerDto").setEventType("Progress")
				.setEntityId(requestTracker.getRequestId()).build();
		Map<String,Object> eventBody = new HashMap<>();
		eventBody.put("progress", progress);
		eventBody.put("hardScore", 0);
		eventBody.put("softScore", 0);
		eventBody.put("msg", progressInfo);
		try {
			eventService.sendEvent(EventScope.AppServer, key, Event.wrap(eventBody),"SchedulingService");
		} catch (Throwable t) {
			logger.error("notifyProgress Error",t);
		}
	}

	private void notifyProgress(ShiftSwapEligibilityRequestTracker requestTracker, int progress, int hardScore,
			int softScore, String progressInfo) {
		ASEventService eventService = getEventService();
		Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM_NOTIFICATION)
				.setTenantId(requestTracker.getTenantId())
				.setEntityClass("QualificationRequestTrackerDto").setEventType("Progress")
				.setEntityId(requestTracker.getRequestId()).build();
		Map<String,Object> eventBody = new HashMap<>();
		eventBody.put("progress", progress);
		eventBody.put("hardScore", 0);
		eventBody.put("softScore", 0);
		eventBody.put("msg", progressInfo);
		try {
			eventService.sendEvent(EventScope.AppServer, key, Event.wrap(eventBody),"SchedulingService");
		} catch (Throwable t) {
			logger.error("notifyProgress Error",t);
		}
	}

}
