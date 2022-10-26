package com.emlogis.engine.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.ProblemFactChange;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.communication.NotificationService;
import com.emlogis.engine.domain.communication.ShiftAssignmentDto;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.driver.internal.ForceCompletionEngine;
import com.emlogis.engine.driver.internal.OptimizationSolverFactory;
import com.emlogis.engine.driver.listeners.AbstractSolverLifecycleListener;
import com.emlogis.engine.driver.listeners.OptimizationUINotifier;
import com.emlogis.engine.solver.drools.score.FlexScoreLevelDirectoryFactoryConfig;
import com.emlogis.engine.solver.drools.score.TempXStreamXMLSolverFactory;

public class OptimizationManager extends AbstractEngineManager {

    // Abort is permitted only when this variable is true
    private boolean abortAllowed;
    protected SolverFactory solverFactory;
    protected TerminationConfig terminationConfig;

    // Location of optimization config file.
    public static final String SOLVER_CONFIG = "com/emlogis/engine/solver/EngineSolverConfig.xml";

    protected volatile Solver solver;

    protected NotificationService notificationService;

    protected Collection<ConstraintMatchTotal> constraintMatchTotals;

    protected int highestScoreRuleLevel = -1;

    public boolean isAbortAllowed() {
	return abortAllowed;
    }

    /**
     * Initialize OptimizationManager with the notification service and
     * default(file configured) execution time and unimproved time.
     * 
     * @param notificationService
     * @throws Exception
     */
    public void initialize(NotificationService notificationService) throws Exception {
	initialize(notificationService, -1, -1, -1);
    }

    /**
     * Initialize the notification service, the optimization solver and add a
     * callback for capturing end-of-excecution information
     * 
     * @param notificationService
     * @param maxExecutionTime
     * @param maxUnimprovedSecondsSpent
     * @throws Exception
     */
    public void initialize(NotificationService notificationService, long maxExecutionTime,
	    int maxUnimprovedSecondsSpent, int highestScoreRuleLevel) throws Exception {
	this.notificationService = notificationService;
	this.highestScoreRuleLevel = highestScoreRuleLevel;

	// Load XML solver configuration and initialize solver
	initializeSolver(SOLVER_CONFIG, maxExecutionTime, maxUnimprovedSecondsSpent);

	// Set listener to notify clients of progress
	initializeNotificationService(notificationService);

	// Add listener to get data at end of optimization
	if (solver instanceof DefaultSolver) {
	    ((DefaultSolver) solver).addPhaseLifecycleListener(new AbstractSolverLifecycleListener() {
		@Override
		public void solvingEnded(DefaultSolverScope solverScope) {
		    solverScope.getScoreDirector().setWorkingSolution(solverScope.getBestSolution());
		    solverScope.getScoreDirector().calculateScore();

		    // Constraints should always be enabled but have this just
		    // in case
		    if (solverScope.getScoreDirector().isConstraintMatchEnabled()) {
			constraintMatchTotals = solverScope.getScoreDirector().getConstraintMatchTotals();
		    }
		}
	    });
	}
    }

    public EmployeeSchedule solve(EmployeeSchedule workingSolution) {
	if (solver == null) {
	    // TODO: Add a notification message here
	    logger.error("Engine Manager not initialized, aborting optimization");
	    return null;
	}

	// Create shift assignment objects for each shift in the schedule
	createShiftAssignments(workingSolution);

	// Create a shift date object for every date in the schedule
	createShiftDateObjects(workingSolution);

	logInitialOptimizationInfo();

	// Begin solving and time the solving process
	StopWatch watch = new StopWatch();
	watch.start();
	abortAllowed = true;
	solver.solve(workingSolution);
	watch.stop();
	logger.debug("Time to solve: " + watch.getTime());

	// Get the best solution
	EmployeeSchedule bestSolution = (EmployeeSchedule) solver.getBestSolution();

	long maxRemainingTime = terminationConfig.getSecondsSpentLimit() - solver.getTimeMillisSpent()/1000;
	
	// Iff force completion is enabled run the Force Completion Engine to fill shifts
	boolean useForceCompletion = workingSolution.getEmployeeRosterInfo().isForceCompletionEnabled();
	if (useForceCompletion) {
	    ForceCompletionEngine fcEngine = new ForceCompletionEngine(highestScoreRuleLevel, constraintMatchTotals, maxRemainingTime);
	    bestSolution = fcEngine.runForceCompletion(bestSolution);
	    constraintMatchTotals = fcEngine.getConstraintMatchTotals();
	}

	return bestSolution;
    }

    /**
     * Add a notification services to the solver to send out messages at each
     * step as well as the end of execution
     * 
     * @param notificationService
     */
    protected void initializeNotificationService(final NotificationService notificationService) {
	if (notificationService != null) {
	    if (solver instanceof DefaultSolver) {
		((DefaultSolver) solver).addPhaseLifecycleListener(new OptimizationUINotifier(notificationService,
			terminationConfig));
	    }
	}
    }

    /**
     * Send start of execution notification message
     */
    protected void logInitialOptimizationInfo() {
	if (notificationService != null) {
	    final long maxAllowedTime = terminationConfig.calculateTimeMillisSpentLimit();
	    // notificationService.notifyProgress(0, -999999, -999999, -999999,
	    // "Starting optimization with a maximum of "
	    // + (int) (maxAllowedTime / 1000) +
	    // " seconds allotted for execution.");
	}
    }

    /**
     * Clear all transient data to get Manager ready for next execution
     */
    public void clear() {
	abortAllowed = false;
	constraintMatchTotals.clear();
    }

    /**
     * Loads configuration from XML and builds the solver and terminationConfig
     * variables.
     */
    protected void initializeSolver(String configPath, long maxExecutionTime, int maxUnimprovedSecondsSpent) {
	solverFactory = OptimizationSolverFactory.createSolverFactory(configPath, highestScoreRuleLevel);

	terminationConfig = solverFactory.getSolverConfig().getTerminationConfig();
	setMaxExecutionTime(maxExecutionTime);
	setMaxUnimprovedSecondsSpent(maxUnimprovedSecondsSpent);

	StopWatch watch = new StopWatch();
	watch.start();
	solver = solverFactory.buildSolver();
	watch.stop();
	logger.debug("Time to build Optimization Solver is: " + watch.getTime());
    }

    /**
     * Sets the maximum execution time (in seconds) of the optimizer
     * 
     * @param timeInSecs
     */
    public void updateTerminationCriteria(long maxExecutionTime, long maxUnimprovedSecondsSpent) {
	if (terminationConfig != null) {
	    Long previousMaxExecutionTime = terminationConfig.getSecondsSpentLimit();
	    Long previousUnimprovedSecondsSpent = terminationConfig.getUnimprovedSecondsSpentLimit();

	    boolean rebuildSolver = false;
	    if (maxExecutionTime > 0 && previousMaxExecutionTime != maxExecutionTime) {
		setMaxExecutionTime(maxExecutionTime);
		rebuildSolver = true;
	    }

	    if (maxUnimprovedSecondsSpent > 0 && previousUnimprovedSecondsSpent != maxUnimprovedSecondsSpent) {
		setMaxUnimprovedSecondsSpent(maxUnimprovedSecondsSpent);
		rebuildSolver = true;
	    }

	    if (rebuildSolver) {
		StopWatch watch = new StopWatch();
		watch.start();
		solver = solverFactory.buildSolver();
		watch.stop();
		logger.debug("Time to rebuild Solver is: " + watch.getTime());
	    }

	}
    }

    /**
     * @return true if solver was instructed to abort
     */
    public boolean isTerminateEarly() {
	return solver != null && solver.isTerminateEarly();
    }

    /**
     * Notifies the solver that it should stop at its earliest convenience. This
     * method returns immediately, but it takes an undetermined time for the
     * solve(Solution) to actually return.
     * 
     * @return
     */
    public boolean abort() {
	return solver.terminateEarly();
    }

    /**
     * Sets the maximum execution time (in seconds) of the optimizer
     * 
     * @param timeInSecs
     */
    public void setMaxExecutionTime(long timeInSecs) {
	if (terminationConfig != null) {
	    // Set Max Time as received from client side
	    if (timeInSecs > 0) {
		terminationConfig.setHoursSpentLimit(0L);
		terminationConfig.setMinutesSpentLimit(0L);
		terminationConfig.setSecondsSpentLimit(timeInSecs);
		terminationConfig.setMillisecondsSpentLimit(0L);
	    }
	}
    }

    /**
     * Sets the unimproved step time limit termination criteria.
     * 
     * If the optimizer spends maximumUnimprovedSecondsSpent without improving
     * the score it will terminate
     * 
     * @param maximumUnimprovedSecondsSpent
     */
    public void setMaxUnimprovedSecondsSpent(long maximumUnimprovedSecondsSpent) {
	if (terminationConfig != null) {
	    if (maximumUnimprovedSecondsSpent > 0) {
		terminationConfig.setUnimprovedHoursSpentLimit(0L);
		terminationConfig.setUnimprovedMinutesSpentLimit(0L);
		terminationConfig.setUnimprovedSecondsSpentLimit((long) maximumUnimprovedSecondsSpent);
		terminationConfig.setUnimprovedMillisecondsSpentLimit(0L);
	    }
	}
    }

    public List<ShiftAssignmentDto> convertToSerializedShiftAssignments(EmployeeSchedule schedule) {
	List<ShiftAssignmentDto> basicShiftAssignments = new ArrayList<>();
	for (ShiftAssignment sa : schedule.getShiftAssignmentList()) {
	    ShiftAssignmentDto ssa = new ShiftAssignmentDto();
	    if (sa.getEmployee() != null) {
		ssa.setShiftId(sa.getShift().getId());
		ssa.setTeamId(sa.getShift().getTeamId());
		ssa.setShiftSkillId(sa.getShift().getSkillId());
		ssa.setShiftStartDateTime(sa.getShift().getShiftStartDateTime());
		ssa.setShiftEndDateTime(sa.getShift().getShiftEndDateTime());
		ssa.setEmployeeId(sa.getEmployeeId());
		ssa.setExcess(sa.isExcessShift());
		ssa.setLocked(sa.isLocked());
		ssa.setEmployeeName(StringUtils.defaultString(sa.getEmployee().getFirstName()) + " "
			+ StringUtils.defaultString(sa.getEmployee().getLastName()));
		basicShiftAssignments.add(ssa);
	    }
	}
	return basicShiftAssignments;
    }

    protected String printConstraintMatches(boolean includeDetails) {
	if (constraintMatchTotals == null || constraintMatchTotals.isEmpty()) {
	    return "";
	}

	StringBuilder constraintMatchesStr = new StringBuilder();
	constraintMatchesStr.append("Constraints: ");
	constraintMatchesStr.append("\n");

	for (ConstraintMatchTotal constraintMatchTotal : constraintMatchTotals) {
	    String constraintName = constraintMatchTotal.getConstraintName();
	    Number weightTotal = constraintMatchTotal.getWeightTotalAsNumber();
	    constraintMatchesStr.append("\t");
	    constraintMatchesStr.append(constraintName + " = " + weightTotal);
	    constraintMatchesStr.append("\n");
	    if (includeDetails) {
		for (ConstraintMatch constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
		    List<Object> justificationList = constraintMatch.getJustificationList();
		    Number weight = constraintMatch.getWeightAsNumber();
		    constraintMatchesStr.append("\t\t");
		    constraintMatchesStr.append(justificationList + " = " + weight);
		    constraintMatchesStr.append("\n");
		}
	    }
	}
	return constraintMatchesStr.toString();
    }

    public String getConstraintMatches(boolean includeDetails) {
	return printConstraintMatches(includeDetails);
    }

    public Collection<ConstraintMatchTotal> getConstraintMatchTotals() {
	return constraintMatchTotals;
    }

    public void setConstraintMatchTotals(Collection<ConstraintMatchTotal> constraintMatchTotals) {
	this.constraintMatchTotals = constraintMatchTotals;
    }

}
