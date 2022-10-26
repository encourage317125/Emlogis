package com.emlogis.engine.drools.scoringrules;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({SeniorityRuleTester.class, ScheduleCostRuleTester.class, CDPreferenceTester.class, CIPreferenceTester.class
	//,OpenShiftsPreferences.class
	, ExcessShiftsPreferences.class, HorizontalClusteringPreferences.class, VerticalClusteringPreferences.class,
	PrimarySkillPreference.class, TeamPreferenceTest.class, TeamScatteringPreferences.class, DoublingUpTester.class})
public class SoftConstraintTest {

}
