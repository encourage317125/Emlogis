package com.emlogis.schedule.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.primint.IntConstraintMatchTotal;

import com.emlogis.engine.api.OptaplannerEngine;
import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.communication.AssignmentResultDto;
import com.emlogis.engine.domain.communication.IntConstraintMatchTotalDto;
import com.emlogis.engine.domain.communication.NotificationService;
import com.emlogis.engine.domain.communication.QualificationResultDto;
import com.emlogis.engine.domain.communication.ScheduleCompletion;
import com.emlogis.engine.domain.communication.ScheduleResult;
import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.communication.ShiftSwapEligibilityResultDto;
import com.emlogis.engine.domain.communication.constraints.ScoreLevelResultDto;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.domain.solver.SolverConstants;
import com.emlogis.engine.driver.OpenShiftEligibilityManager;
import com.emlogis.engine.driver.OptimizationManager;
import com.emlogis.engine.driver.QualificationManager;
import com.emlogis.engine.driver.ShiftSwapEligibilityManager;

public class OptaplannerEngineImpl implements OptaplannerEngine {

    private final static Logger logger = Logger.getLogger(OptaplannerEngineImpl.class.getSimpleName());

    private static final int STATUS_OK = 0;
    private static final int STATUS_ERROR = 1;
    private static final int STATUS_ABORTED = 2;

    private boolean abortSuccessful = false; // Successful

    private OptimizationManager optimizationManager;
    private OpenShiftEligibilityManager openShiftEligibilityManager;
    private ShiftSwapEligibilityManager shiftSwapEligibilityManager;
    private QualificationManager qualificationManager; // TODO: Find a better
						       // way to
						       // organize the class
						       // relationships

    public boolean abortAllowed() {
	return optimizationManager != null && optimizationManager.isAbortAllowed();
    }

    @Override
    public ScheduleResult findAssignments(EmployeeSchedule schedule, NotificationService notificationService,
	    boolean includeDetails) {
	int status = STATUS_OK;
	abortSuccessful = false; // Reset the abort flag

	ScheduleResult result = new ScheduleResult();

	long executionTimeInSecs = schedule.getMaxComputationTime();
	int maxUnimprovedSecondsSpent = schedule.getMaximumUnimprovedSecondsSpent();
	int highestScoreRuleLevel = schedule.getEmployeeRosterInfo().getHighestRuleLevel();
	EmployeeSchedule solved;
	try {
	    StopWatch watch = new StopWatch();
	    watch.start();
	    if (optimizationManager == null) {
		optimizationManager = new OptimizationManager();
		optimizationManager.initialize(notificationService, executionTimeInSecs, maxUnimprovedSecondsSpent,
			highestScoreRuleLevel);
	    }

	    optimizationManager.setIncludeResultDetails(includeDetails);

	    optimizationManager.updateTerminationCriteria(executionTimeInSecs, maxUnimprovedSecondsSpent);
	    logger.info("Engine initialization time: " + watch.getTime());
	    watch.split();
	    solved = optimizationManager.solve(schedule);
	    watch.stop();
	    logger.info("Engine solving time: " + watch.getSplitTime());
	} catch (Exception e) {
	    e.printStackTrace();
	    logger.error("findAssignments failed", e);
	    // If an exception is thrown it should be returned to the client
	    result.setCompletion(ScheduleCompletion.Error);
	    result.setCompletionInfo(e.getLocalizedMessage());
	    return result;
	}

	if (optimizationManager.isTerminateEarly()) {
	    status = STATUS_ABORTED;
	}

	List<ShiftAssignmentDto> scheduleResult = optimizationManager.convertToSerializedShiftAssignments(solved);
	BendableScore finalScore = solved.getScore();

	switch (status) {
	case STATUS_OK:
	    result.setCompletion(ScheduleCompletion.OK);
	    result.setCompletionInfo("Optimization Complete");
	    break;
	case STATUS_ABORTED:
	    result.setCompletion(ScheduleCompletion.Aborted);
	    result.setCompletionInfo("Optimization aborted - Returning best found solution)");
	    break;
	}
	AssignmentResultDto resultData = new AssignmentResultDto();
	resultData.setId("Completed Schedule Result");
	resultData.setShiftAssignments(scheduleResult);

	Collection<ConstraintMatchTotal> constraints = optimizationManager.getConstraintMatchTotals();

	// Set the score arrays in the AssighmentResultDto
	convertToScoreLevelResultList(constraints, resultData);

	// Get ConstraintMatchTotals and convert to DTO
	if (includeDetails) {
	    Collection<IntConstraintMatchTotalDto> dtoCollection = convertConstraintMatches(constraints);
	    resultData.setConstraintMatchTotals(dtoCollection);
	}
	result.setResult(resultData);

	return result;
    }

    @Override
    public ScheduleResult checkQualification(EmployeeSchedule schedule, NotificationService notificationService,
	    boolean includeDetails) {
	int status = STATUS_OK;
	abortSuccessful = false; // Reset the abort flag

	ScheduleResult result = new ScheduleResult();
	BendableScore scheduleScore;

	try {
	    if (qualificationManager == null) {
		qualificationManager = new QualificationManager();
		qualificationManager.initialize();
	    }

	    qualificationManager.setIncludeResultDetails(includeDetails);

	    scheduleScore = qualificationManager.qualifyScheduleAssignments(schedule);
	} catch (Exception e) {
	    logger.error("qualification failed", e);
	    // If an exception is thrown it should be returned to the client
	    result.setCompletion(ScheduleCompletion.Error);
	    result.setCompletionInfo(e.getLocalizedMessage());
	    return result;
	}

	switch (status) {
	case STATUS_OK:
	    result.setCompletion(ScheduleCompletion.OK);
	    result.setCompletionInfo("Qualification Complete");
	    break;
	case STATUS_ABORTED:
	    result.setCompletion(ScheduleCompletion.Aborted);
	    result.setCompletionInfo("Qualification aborted - Could not qualify the schedule)");
	    break;
	}
	QualificationResultDto resultData = new QualificationResultDto();

	// Retrieve and convert qualification results
	Collection<ShiftQualificationDto> shiftResults = qualificationManager.getShiftQualificationResults();
	resultData.setQualifyingShifts(shiftResults);

	convertToScoreLevelResultList(scheduleScore, resultData);

	result.setResult(resultData);

	qualificationManager.clear(); // Clear transient data

	return result;
    }

    @Override
    public ScheduleResult getOpenShiftEligibility(EmployeeSchedule schedule, NotificationService notificationService,
	    boolean includeDetails) {
	int status = STATUS_OK;
	abortSuccessful = false; // Reset the abort flag

	ScheduleResult result = new ScheduleResult();
	Collection<ShiftQualificationDto> shiftResults;
	try {
	    if (openShiftEligibilityManager == null) {
		openShiftEligibilityManager = new OpenShiftEligibilityManager();
		openShiftEligibilityManager.initialize(notificationService);
	    }

	    openShiftEligibilityManager.setIncludeResultDetails(includeDetails);

	    shiftResults = openShiftEligibilityManager.getAssignmentsEligibility(schedule);
	} catch (Exception e) {
	    logger.error("shift eligibility failed:", e);
	    // If an exception is thrown it should be returned to the client
	    result.setCompletion(ScheduleCompletion.Error);
	    result.setCompletionInfo(e.getLocalizedMessage());
	    return result;
	}

	switch (status) {
	case STATUS_OK:
	    result.setCompletion(ScheduleCompletion.OK);
	    result.setCompletionInfo("Open Shift Eligibility Complete");
	    break;
	case STATUS_ABORTED:
	    result.setCompletion(ScheduleCompletion.Aborted);
	    result.setCompletionInfo("Open Shift Eligibility aborted - Could not run eligibility process)");
	    break;
	}
	QualificationResultDto resultData = new QualificationResultDto();

	// Retrieve and convert qualification results
	resultData.setQualifyingShifts(shiftResults);

	result.setResult(resultData);

	openShiftEligibilityManager.clear(); // Clear transient data

	return result;
    }

    @Override
    public ScheduleResult getShiftSwapEligibility(EmployeeSchedule schedule, NotificationService notificationService,
	    boolean includeDetails) {
	int status = STATUS_OK;
	abortSuccessful = false; // Reset the abort flag

	ScheduleResult result = new ScheduleResult();
	Map<String, Collection<ShiftQualificationDto>> shiftResults;
	try {
	    if (shiftSwapEligibilityManager == null) {
		shiftSwapEligibilityManager = new ShiftSwapEligibilityManager();
		shiftSwapEligibilityManager.initialize(notificationService);
	    }

	    shiftSwapEligibilityManager.setIncludeResultDetails(includeDetails);

	    shiftResults = shiftSwapEligibilityManager.getShiftSwapEligibility(schedule);
	} catch (Exception e) {
	    logger.error("Swap Eligibility failed",e);
	    // If an exception is thrown it should be returned to the client
	    result.setCompletion(ScheduleCompletion.Error);
	    result.setCompletionInfo(e.getLocalizedMessage());
	    return result;
	}

	switch (status) {
	case STATUS_OK:
	    result.setCompletion(ScheduleCompletion.OK);
	    result.setCompletionInfo("Shift Swap Eligibility Complete");
	    break;
	case STATUS_ABORTED:
	    result.setCompletion(ScheduleCompletion.Aborted);
	    result.setCompletionInfo("Shift Swap Eligibility aborted - Could not run eligibility process)");
	    break;
	}
	ShiftSwapEligibilityResultDto resultData = new ShiftSwapEligibilityResultDto();

	// Retrieve and convert qualification results
	resultData.setQualifyingShifts(shiftResults);

	result.setResult(resultData);

	shiftSwapEligibilityManager.clear(); // Clear transient data

	return result;
    }

    public Collection<IntConstraintMatchTotalDto> convertConstraintMatches(Collection<ConstraintMatchTotal> constraints) {
	Collection<IntConstraintMatchTotalDto> dtoCollection = new ArrayList<>();
	for (ConstraintMatchTotal cmt : constraints) {
	    if (cmt instanceof IntConstraintMatchTotal) {
		IntConstraintMatchTotal icmt = (IntConstraintMatchTotal) cmt;
		IntConstraintMatchTotalDto convertedIntConstraint = new IntConstraintMatchTotalDto(icmt);
		dtoCollection.add(convertedIntConstraint);
	    }
	}
	return dtoCollection;
    }

    private void convertToScoreLevelResultList(Collection<ConstraintMatchTotal> constraints,
	    AssignmentResultDto resultData) {
	List<ScoreLevelResultDto> hardScoreList = fillScoreLevelList(SolverConstants.NUM_HARD_LEVELS);
	List<ScoreLevelResultDto> softScoreList = fillScoreLevelList(SolverConstants.NUM_SOFT_LEVELS);

	for (ConstraintMatchTotal cmt : constraints) {
	    if (cmt instanceof IntConstraintMatchTotal) {
		IntConstraintMatchTotal icmt = (IntConstraintMatchTotal) cmt;

		RuleName constraintName = RuleName.fromString(icmt.getConstraintName());
		if (constraintName == null)
		    continue;

		int scoreLevel = icmt.getScoreLevel();
		int value = icmt.getWeightTotal();
		if (scoreLevel > SolverConstants.NUM_HARD_LEVELS - 1) {
		    // Soft Score, subtract the num of hard levels to get proper
		    // index
		    // in the soft score array
		    scoreLevel = scoreLevel - SolverConstants.NUM_HARD_LEVELS;
		    ScoreLevelResultDto softScore = softScoreList.get(scoreLevel);
		    softScore.addScoreLevelDetail(constraintName, value);
		} else {
		    // Hard Score
		    ScoreLevelResultDto hardScore = hardScoreList.get(scoreLevel);
		    hardScore.addScoreLevelDetail(constraintName, value);
		}
	    }
	}

	resultData.setHardScores(hardScoreList);
	resultData.setSoftScores(softScoreList);
    }

    private void convertToScoreLevelResultList(BendableScore score, QualificationResultDto resultData) {
	List<ScoreLevelResultDto> hardScoreList = fillScoreLevelList(SolverConstants.NUM_HARD_LEVELS);
	List<ScoreLevelResultDto> softScoreList = fillScoreLevelList(SolverConstants.NUM_SOFT_LEVELS);

	for (int i = 0; i < hardScoreList.size(); i++) {
	    hardScoreList.get(i).setTotalScoreValue(score.getHardScore(i));
	}

	for (int i = 0; i < softScoreList.size(); i++) {
	    softScoreList.get(i).setTotalScoreValue(score.getSoftScore(i));
	}

	resultData.setHardScores(hardScoreList);
	resultData.setSoftScores(softScoreList);
    }

    /**
     * Creates a list of ScoreLevelResultDto objects filled to capacity with
     * empty Dto objects
     * 
     * @param capacity
     * @return
     */
    private List<ScoreLevelResultDto> fillScoreLevelList(int capacity) {
	List<ScoreLevelResultDto> scoreList = new ArrayList<>(capacity);
	for (int i = 0; i < capacity; i++) {
	    scoreList.add(new ScoreLevelResultDto());
	}
	return scoreList;
    }

    @Override
    public void abort() {
	abortSuccessful = true;
	optimizationManager.abort();
    }

    public boolean abortSuccessful() {
	return abortSuccessful;
    }

}
