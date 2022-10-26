package com.emlogis.engine.solver.drools.score;

import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("flexScoreDirectorFactory")
public class FlexScoreLevelDirectoryFactoryConfig extends ScoreDirectorFactoryConfig {
    
    @XStreamAlias("numOfScoreLevels")
    private int numOfScoreLevels = -1; 
    
    public int getNumOfScoreLevels() {
        return numOfScoreLevels;
    }

    public void setNumOfScoreLevels(int numOfScoreLevels) {
        this.numOfScoreLevels = numOfScoreLevels;
    }

    @Override
    public ScoreDefinition buildScoreDefinition() {
        ScoreDefinition scoreDef = super.buildScoreDefinition();
        if(scoreDef instanceof QualificationScoreDefinition){
            if(numOfScoreLevels > 0){
        	((QualificationScoreDefinition) scoreDef).setSoftScoreLevels(numOfScoreLevels);
            }
        }
        return scoreDef;
    }
    
}
