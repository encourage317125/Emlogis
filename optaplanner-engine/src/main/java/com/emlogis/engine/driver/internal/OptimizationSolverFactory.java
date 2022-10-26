package com.emlogis.engine.driver.internal;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emlogis.engine.solver.drools.score.FlexScoreLevelDirectoryFactoryConfig;
import com.emlogis.engine.solver.drools.score.TempXStreamXMLSolverFactory;

public class OptimizationSolverFactory {
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static SolverFactory createSolverFactory(String configPath, int highestScoreRuleLevel) {
	TempXStreamXMLSolverFactory xmlSolverFactory = new TempXStreamXMLSolverFactory();
	xmlSolverFactory.aliasAttribute(SolverConfig.class, "scoreDirectorFactoryConfig", "flexScoreDirectorFactory");
	xmlSolverFactory.alias("flexScoreDirectorFactory", FlexScoreLevelDirectoryFactoryConfig.class);
	xmlSolverFactory.addDefaultImplementation(FlexScoreLevelDirectoryFactoryConfig.class,
		ScoreDirectorFactoryConfig.class);
	xmlSolverFactory.configure(configPath);

	ScoreDirectorFactoryConfig scoreFactoryConfig = xmlSolverFactory.getSolverConfig()
		.getScoreDirectorFactoryConfig();
	if (scoreFactoryConfig instanceof FlexScoreLevelDirectoryFactoryConfig) {
	    ((FlexScoreLevelDirectoryFactoryConfig) scoreFactoryConfig).setNumOfScoreLevels(highestScoreRuleLevel + 1);
	}

	return xmlSolverFactory;
    }
}
