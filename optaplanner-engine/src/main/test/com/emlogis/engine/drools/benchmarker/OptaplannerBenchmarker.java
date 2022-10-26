package com.emlogis.engine.drools.benchmarker;

import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;

import com.emlogis.engine.solver.drools.score.FlexScoreLevelDirectoryFactoryConfig;
import com.emlogis.engine.solver.drools.score.TempFreemarkerXmlPlannerBenchmarkFactory;

public class OptaplannerBenchmarker {
    public final static String BENCHMARKER_CONFIG_NAME = "com/emlogis/engine/solver/benchmarker/benchmarker.xml";

    public static void main(String[] args) {

	TempFreemarkerXmlPlannerBenchmarkFactory plannerBenchmarkFactory = new TempFreemarkerXmlPlannerBenchmarkFactory();
	plannerBenchmarkFactory.aliasAttribute(SolverConfig.class, "scoreDirectorFactoryConfig", "flexScoreDirectorFactory");
	plannerBenchmarkFactory.alias("flexScoreDirectorFactory", FlexScoreLevelDirectoryFactoryConfig.class);
	plannerBenchmarkFactory.addDefaultImplementation(FlexScoreLevelDirectoryFactoryConfig.class, ScoreDirectorFactoryConfig.class);
	
	plannerBenchmarkFactory.configure(BENCHMARKER_CONFIG_NAME);
	
	PlannerBenchmark plannerBenchmark = plannerBenchmarkFactory.buildPlannerBenchmark();
	plannerBenchmark.benchmark();

    }
}
