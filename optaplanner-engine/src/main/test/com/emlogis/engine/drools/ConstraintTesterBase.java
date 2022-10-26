package com.emlogis.engine.drools;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.DayOfWeek;
import com.emlogis.engine.domain.EmployeeRosterInfo;
import com.emlogis.engine.domain.WeekendDefinition;
import com.emlogis.engine.domain.dto.EmployeeRosterDto;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.engine.solver.drools.score.QualificationScoreHolder;

public abstract class ConstraintTesterBase extends TestDataUtility {
	protected KieSession kSession;
	protected EmployeeRosterInfo rosterInfo;

	// Rule Name Constants


	@Before
	public void setUp() {
		// load up the knowledge base
		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();
		kSession = kContainer.newKieSession("rules-test");
		kSession.setGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY, new QualificationScoreHolder(true));

		rosterInfo = new EmployeeRosterInfo();
		rosterInfo.setFirstDayOfWeek(DayOfWeek.SUNDAY);
		rosterInfo.setWeekendDefinition(WeekendDefinition.SATURDAY_SUNDAY);
		setUpRuleWeights();
		setUpScoringRuleScoreLevels();
		
		loadRosterInfo();

		kSession.insert(rosterInfo);
	}

	@After
	public void tearDown() {
		kSession.destroy();
	}

	protected abstract void loadRosterInfo();

	/**
	 * Set the weight of each rule to 1 by default
	 * 
	 */
	protected void setUpRuleWeights() {
		// Set all values to a default of 1 
		for(RuleName rule : RuleName.values()){
			rosterInfo.putRuleWeightMultiplier(rule, 1);
		}
		
		// Set non-default weight values
		rosterInfo.putRuleWeightMultiplier(RuleName.SKILL_MATCH_RULE, 40);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT, 10);
		rosterInfo.putRuleWeightMultiplier(RuleName.TEAM_ASSOCIATION_CONSTRAINT_FLOAT, 10);
		rosterInfo.putRuleWeightMultiplier(RuleName.OVERLAPPING_SHIFTS_RULE, 10);	
	}
	
	/**
	 * Set default values for the soft scoring rules
	 * in future this will be loaded from the schedule
	 * generation request
	 * 
	 * @param rosterInfo
	 */
	protected void setUpScoringRuleScoreLevels(){
		rosterInfo.setScoringRulesToScoreLevelMap(new HashMap<RuleName, Integer>());
		rosterInfo.putScoringRuleScoreLevel(RuleName.CD_PREFERENCE_RULE, 2);
		rosterInfo.putScoringRuleScoreLevel(RuleName.CI_PREFERENCE_RULE, 2);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SCHEDULE_COST_RULE, 0);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SCHEDULE_OVERTIME_RULE, 1);
		rosterInfo.putScoringRuleScoreLevel(RuleName.SENIORITY_RULE, 3);
		rosterInfo.putScoringRuleScoreLevel(RuleName.EXTRA_SHIFT_RULE, 3);
		rosterInfo.putScoringRuleScoreLevel(RuleName.PREFER_TEAM_SCATTERING_RULE, 3);
	}

}
