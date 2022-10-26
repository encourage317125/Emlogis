/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emlogis.engine.solver.drools.score;

import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.config.score.trend.InitializingScoreTrendLevel;
import org.optaplanner.core.impl.score.definition.AbstractFeasibilityScoreDefinition;
import org.optaplanner.core.impl.score.trend.InitializingScoreTrend;

import com.emlogis.engine.domain.solver.SolverConstants;

public class QualificationScoreDefinition extends AbstractFeasibilityScoreDefinition<BendableScore> {

    // Default value can be changed
    private int softScoreLevels = SolverConstants.NUM_SOFT_LEVELS;

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public int getLevelsSize() {
	return SolverConstants.NUM_HARD_LEVELS + softScoreLevels;
    }

    @Override
    public int getFeasibleLevelsSize() {
	return 1;
    }

    public Class<BendableScore> getScoreClass() {
	return BendableScore.class;
    }

    public BendableScore parseScore(String scoreString) {
	return BendableScore.parseScore(SolverConstants.HARD_CONSTRAINT_LEVEL, softScoreLevels, scoreString);
    }

    public QualificationScoreHolder buildScoreHolder(boolean constraintMatchEnabled) {
	return new QualificationScoreHolder(true, softScoreLevels); // Always
								    // store
								    // constraints
    }

    public BendableScore buildOptimisticBound(InitializingScoreTrend initializingScoreTrend, BendableScore score) {
	InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.getTrendLevels();
	int[] hardScores = new int[SolverConstants.NUM_HARD_LEVELS];
	hardScores[0] = (trendLevels[0] == InitializingScoreTrendLevel.ONLY_UP ? score
		.getHardScore(SolverConstants.HARD_CONSTRAINT_LEVEL) : Integer.MAX_VALUE);
	hardScores[1] = (trendLevels[1] == InitializingScoreTrendLevel.ONLY_UP ? score
		.getHardScore(SolverConstants.OPEN_SHIFTS_CONSTRAINT_LEVEL) : Integer.MAX_VALUE);
	hardScores[2] = (trendLevels[2] == InitializingScoreTrendLevel.ONLY_UP ? score
		.getHardScore(SolverConstants.MED_CONSTRAINT_LEVEL) : Integer.MAX_VALUE);

	int softScores[] = new int[SolverConstants.NUM_SOFT_LEVELS];
	for (int i = 0; i < softScores.length; i++) {
	    softScores[i] = trendLevels[i+SolverConstants.NUM_HARD_LEVELS] == InitializingScoreTrendLevel.ONLY_DOWN ? score.getSoftScore(0)
			: Integer.MAX_VALUE;
	    
	}
	return BendableScore.valueOf(hardScores, softScores);
    }

    public BendableScore buildPessimisticBound(InitializingScoreTrend initializingScoreTrend, BendableScore score) {
	InitializingScoreTrendLevel[] trendLevels = initializingScoreTrend.getTrendLevels();
	int[] hardScores = new int[SolverConstants.NUM_HARD_LEVELS];
	hardScores[0] = (trendLevels[0] == InitializingScoreTrendLevel.ONLY_UP ? score
		.getHardScore(SolverConstants.HARD_CONSTRAINT_LEVEL) : Integer.MIN_VALUE);
	hardScores[1] = (trendLevels[1] == InitializingScoreTrendLevel.ONLY_UP ? score
		.getHardScore(SolverConstants.OPEN_SHIFTS_CONSTRAINT_LEVEL) : Integer.MIN_VALUE);
	hardScores[2] = (trendLevels[2] == InitializingScoreTrendLevel.ONLY_UP ? score
		.getHardScore(SolverConstants.MED_CONSTRAINT_LEVEL) : Integer.MIN_VALUE);

	int softScores[] = new int[SolverConstants.NUM_SOFT_LEVELS];
	for (int i = 0; i < softScores.length; i++) {
	    softScores[i] = trendLevels[i+SolverConstants.NUM_HARD_LEVELS] == InitializingScoreTrendLevel.ONLY_DOWN ? score.getSoftScore(0)
			: Integer.MIN_VALUE;
	    
	}
	return BendableScore.valueOf(hardScores, softScores);
    }

    public int getSoftScoreLevels() {
	return softScoreLevels;
    }

    public void setSoftScoreLevels(int softScoreLevels) {
	this.softScoreLevels = softScoreLevels;
    }

}
