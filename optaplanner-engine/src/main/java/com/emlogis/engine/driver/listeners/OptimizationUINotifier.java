package com.emlogis.engine.driver.listeners;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

import com.emlogis.engine.domain.communication.NotificationService;

public class OptimizationUINotifier extends AbstractSolverLifecycleListener {
	private NotificationService notificationService;
	private TerminationConfig terminationConfig;
	long lastMessageSentTime = 0;

	public OptimizationUINotifier(NotificationService notificationService,
			TerminationConfig terminationConfig) {
		this.notificationService = notificationService;
		this.terminationConfig = terminationConfig;
	}

	@Override
	public void solvingEnded(DefaultSolverScope solverScope) {
		Score score = solverScope.getBestScore();
		int softScore = -999999;
		int hardScore = -999999;

		String msg = "Optimization finished with score of "
				+ solverScope.getBestScore() + " after "
				+ (int) (solverScope.calculateTimeMillisSpent() / 1000)
				+ " seconds";

		if (score instanceof BendableScore) {
			hardScore = sumScores((BendableScore) score, true);
			softScore = sumScores((BendableScore) score, false);
		}
		notificationService.notifyProgress(100, hardScore, softScore, msg);
	}

	@Override
	public void stepEnded(AbstractStepScope stepScope) {
		long maxAllowedTime = terminationConfig.calculateTimeMillisSpentLimit();
		long timeSpent = stepScope.getPhaseScope()
				.calculateSolverTimeMillisSpent();
		Score score = stepScope.getScore();
		int percentage = (int) (timeSpent * 100.0 / maxAllowedTime + 0.5);
		if (score instanceof BendableScore) {
			// Limit sending notifications to max of 1 per second
			if (System.currentTimeMillis() - lastMessageSentTime > 1000) {
				lastMessageSentTime = System.currentTimeMillis();
				int hardScore = sumScores((BendableScore) score, true);
				int softScore = sumScores((BendableScore) score, false);

				notificationService.notifyProgress(percentage,
						hardScore, softScore,
						"running...");
			}
		} else {
			notificationService.notifyProgress(percentage, -999999, -999999,
					score.toString());
		}
	}

	/**
	 * Ugly workaround since BendableScore does not return neither a 
	 * full sum for the hard score/soft score nor an array of scores
	 * 
	 * @param score 
	 * @param isHardScore iff true returns hard score otherwise soft score
	 * @return Sum of either hard or soft scores
	 */
	private int sumScores(BendableScore score, boolean isHardScore) {
		int sum = 0;
		if (isHardScore) {
			for (int i = 0; i < score.getHardLevelsSize(); i++) {
				sum += score.getHardScore(i);
			}
		} else {

			for (int i = 0; i < score.getSoftLevelsSize(); i++) {
				sum += score.getSoftScore(i);
			}
		}

		return sum;
	}
}
