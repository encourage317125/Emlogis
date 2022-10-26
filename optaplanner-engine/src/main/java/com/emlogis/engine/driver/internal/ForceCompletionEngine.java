package com.emlogis.engine.driver.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.time.StopWatch;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.Shift;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.driver.listeners.AbstractSolverLifecycleListener;

/**
 * @author code4dc
 *
 */
/**
 * @author code4dc
 *
 */
/**
 * @author code4dc
 *
 */
public class ForceCompletionEngine {
	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	public static final String FORCE_COMPLETION_CONFIG = "com/emlogis/engine/solver/ForceCompletionSolverConfig.xml";
	private volatile Solver forceCompletionSolver;

	private Collection<ConstraintMatchTotal> constraintMatchTotals;

	private int highestScoreRuleLevel;

	private long remainingExcecutionTime;

	public ForceCompletionEngine(int highestScoreRuleLevel,
			Collection<ConstraintMatchTotal> constraintMatchTotals,
			long maxExecutionTime) {
		this.highestScoreRuleLevel = highestScoreRuleLevel;
		this.constraintMatchTotals = constraintMatchTotals;
		this.remainingExcecutionTime = maxExecutionTime;
	}

	/**
	 * 
	 * Initialize the solver using the FORCE COMPLETION configuration
	 * 
	 */
	protected void initializeForceCompletionSolver() {
		SolverFactory forceCompletionSolverFactory = OptimizationSolverFactory
				.createSolverFactory(FORCE_COMPLETION_CONFIG,
						highestScoreRuleLevel);

		StopWatch watch = new StopWatch();
		watch.start();
		forceCompletionSolver = forceCompletionSolverFactory.buildSolver();
		watch.stop();
		logger.debug("Time to build Force Completion Solver is: "
				+ watch.getTime());

		// Add listener to get data at end of optimization
		if (forceCompletionSolver instanceof DefaultSolver) {
			((DefaultSolver) forceCompletionSolver)
					.addPhaseLifecycleListener(new AbstractSolverLifecycleListener() {
						@Override
						public void solvingEnded(DefaultSolverScope solverScope) {
							solverScope.getScoreDirector().setWorkingSolution(
									solverScope.getBestSolution());
							solverScope.getScoreDirector().calculateScore();

							// Constraints should always be enabled but have
							// this just
							// in case
							if (solverScope.getScoreDirector()
									.isConstraintMatchEnabled()) {
								constraintMatchTotals = solverScope
										.getScoreDirector()
										.getConstraintMatchTotals();
							}
						}
					});
		}

	}

	public EmployeeSchedule runForceCompletion(EmployeeSchedule schedule) {
		initializeForceCompletionSolver();

		// Lock all existing shifts
		for (ShiftAssignment shiftAssignment : schedule
				.getShiftAssignmentList()) {
			shiftAssignment.setLocked(true);
		}

		int weightOfMinHourConstraints = weightOfMinHourConstraintsViolated(constraintMatchTotals);
		logger.trace("Force completion detected: "
				+ weightOfMinHourConstraints + " min hours constraints");

		List<Shift> originalShifts = schedule.getShiftList();
		int originalSize = originalShifts.size();

		int fcRound = 1;
		// End loop when no more minHour constraints are left or timeout is
		// reached
		while (weightOfMinHourConstraints < 0 && remainingExcecutionTime > 0) {
			logger.debug("Starting Force Completion Round: " + fcRound);
			String forceCompletionShiftPrefix = Shift.FORCE_COMPLETION_PREFIX
					+ fcRound + "-";
			// Iterate over existing shifts and add a Force Completion
			// shift for each non-excess shift
			for (int i = 0; i < originalSize; i++) {
				if (originalShifts.get(i).isExcessShift())
					continue;

				Shift forceCompletionShift = new Shift(originalShifts.get(i));
				forceCompletionShift.setId(forceCompletionShiftPrefix
						+ forceCompletionShift.getId());
				forceCompletionShift.setExcessShift(true);
				schedule.getShiftList().add(forceCompletionShift);

				ShiftAssignment forceCompletionShiftAssignment = new ShiftAssignment();
				forceCompletionShiftAssignment.setShift(forceCompletionShift);
				schedule.getShiftAssignmentList().add(
						forceCompletionShiftAssignment);
			}
			forceCompletionSolver.solve(schedule);
			logger.debug("Finished Force Completion Round: " + fcRound);

			// Subtract each rounds execution time from the maxExecutionTime
			remainingExcecutionTime -= forceCompletionSolver
					.getTimeMillisSpent() / 1000;

			fcRound++;

			int newRoundSumOfMinConstraint = weightOfMinHourConstraintsViolated(constraintMatchTotals);
			logger.trace("Force completion detected: "
					+ newRoundSumOfMinConstraint
					+ " min hours constraints after round " + fcRound);

			// End execution if force completion round did not improve score
			if (weightOfMinHourConstraints >= newRoundSumOfMinConstraint) {
				logger.debug("Ending force completion due to lack of improvement during previous round");
				break;
			}

			weightOfMinHourConstraints = newRoundSumOfMinConstraint;
		}

		if (remainingExcecutionTime <= 0) {
			logger.debug("Ending Force Completion after reaching maximum execution time limit");
		}
		
		// Force completion was not run
		if(fcRound == 1){
			logger.debug("Force completion was not executed, returning original schedule");
			return schedule; //return the original schedule
		}

		Solution<BendableScore> bestSolution = forceCompletionSolver
				.getBestSolution();
		return (EmployeeSchedule) bestSolution;
	}

	public Collection<ConstraintMatchTotal> getConstraintMatchTotals() {
		return constraintMatchTotals;
	}

	public void setConstraintMatchTotals(
			Collection<ConstraintMatchTotal> constraintMatchTotals) {
		this.constraintMatchTotals = constraintMatchTotals;
	}

	private int weightOfMinHourConstraintsViolated(
			Collection<ConstraintMatchTotal> constraintMatchTotals) {
		List<RuleName> minConstraintRules = Arrays.asList(
				RuleName.MIN_HOURS_PER_DAY_CONSTRAINT,
				RuleName.MIN_HOURS_PER_WEEK_CONSTRAINT,
				RuleName.MIN_HOURS_PER_WEEK_PRIME_SKILL_CONSTRAINT);
		return sumOfMatchingConstraintWeights(minConstraintRules, constraintMatchTotals);
	}

	
	/*
	 * Return number of constraint matching any of the constraintNames
	 */
	private int sumOfMatchingConstraintWeights(final List<RuleName> constraintNames,
			Collection<ConstraintMatchTotal> constraintMatches) {
		return CollectionUtils.forAllDo(constraintMatches, new Closure<ConstraintMatchTotal>() {
			 private int constraintTotals = 0;
			@Override
			public void execute(ConstraintMatchTotal arg0) {
				if(constraintNames.contains(RuleName.fromString(arg0.getConstraintName()))){
					constraintTotals += arg0.getWeightTotalAsNumber().intValue();
				}
			}
			
			public int getTotal(){ return constraintTotals; }
			
		}).getTotal();
	}

}
