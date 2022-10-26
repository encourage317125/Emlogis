package com.emlogis.engine.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.director.drools.DroolsScoreDirector;

import com.emlogis.engine.domain.EmployeeSchedule;
import com.emlogis.engine.domain.ShiftAssignment;
import com.emlogis.engine.domain.communication.NotificationService;
import com.emlogis.engine.domain.communication.ShiftQualificationDto;
import com.emlogis.engine.domain.communication.constraints.ShiftConstraintDto;
import com.emlogis.engine.solver.drools.score.QualificationScoreHolder;

public class QualificationManager extends AbstractEngineManager{
	protected KieSession kSession;
	protected Collection<ShiftQualificationDto> shiftQualificationResults;
	protected NotificationService notificationService;

	/**
	 * Set up the Rules Base using the definition found in the kmodule.xml
	 * file. 
	 * 
	 * Reports on time it takes to initialize
	 * 
	 * @throws Exception
	 */
	public void initialize(NotificationService notificationService) throws Exception {
		StopWatch watch = new StopWatch();
		watch.start();
		
		// Set up Kie Knowledge base based on kmodule.xml file
		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();
		kSession = kContainer.newKieSession("ksession-rules");
		
		this.notificationService = notificationService;
		
		watch.stop();
		logger.debug("Time to build Qualification Session is: " + watch.getTime());
	}
	

	/**
	 * Set up the Rules Base using the definition found in the kmodule.xml
	 * file. 
	 * 
	 * Reports on time it takes to initialize
	 * 
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		initialize(null);
	}
	
	/**
	 * Qualifies all ShiftAssignments whose corresponding shifts have the 
	 * beingQualified flag set to true. 
	 * 
	 * Returns the schedule score and a map containing each ShiftAssignment
	 * being qualified and a list of constraints violated by the assignment. 
	 *
	 * @param schedule
	 * @return
	 */
	public BendableScore qualifyScheduleAssignments(EmployeeSchedule schedule){
		StopWatch watch = new StopWatch();
		watch.start();
		loadScheduleData(schedule);
		
		// Get list of shifts being qualified and initialize QualfiicationScoreHolder using this list
		Collection<ShiftAssignment> shiftsBeingQualfied = getShiftAssignmentsBeingQualified(schedule.getShiftAssignmentList());
		kSession.setGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY, new QualificationScoreHolder(true, shiftsBeingQualfied));
		
		//ATTACK!! Fire the rules in order to get schedule conflicts
		kSession.fireAllRules();
		
		// Retrieve scoreHolder global containing score and constraints
		QualificationScoreHolder scoreHolder = (QualificationScoreHolder) kSession.getGlobal(DroolsScoreDirector.GLOBAL_SCORE_HOLDER_KEY);
		BendableScore score = (BendableScore) scoreHolder.extractScore();
		shiftQualificationResults = convertQualificationResults(scoreHolder.getShiftConstraintQualificationMap());

		logger.info("Scores : " + score.toString());
		
		watch.stop();
		logger.info("Qualification for " + shiftsBeingQualfied.size() + " shift assigments completed in "
					+ watch.getTime() + " ms");
		
		return score;
	}
	
	
	protected void loadScheduleData(EmployeeSchedule schedule){
		// Clear shift constraint map from previous run
		if (shiftQualificationResults != null) {
			shiftQualificationResults.clear();
		}
		
		createShiftDateObjects(schedule);
		
		// Create a working FactHandle for every schedule fact and entity
		Collection<? extends Object> problemFacts = schedule.getProblemFacts();
		Collection<ShiftAssignment> problemEntities = schedule.getShiftAssignmentList();

		// Populate schedule facts
		for(Object fact : problemFacts) {
			kSession.insert(fact);
		}

		// Populate schedule entities
		for(Object fact : problemEntities) {
			kSession.insert(fact);
		}
	}
	
	/**
	 * Return Map of ShiftConstraintDto for each Shift Assignment
	 * 
	 * @return
	 */
	public Collection<ShiftQualificationDto> getShiftQualificationResults(){
		return shiftQualificationResults;
	}
	
	/**
	 * Go through the working memory and clear 
	 * all facts that were inserted. 
	 * 
	 * Constraints are cleared at the start of each run to avoid having
	 * to clone each ShiftQualificationDto object when returning
	 * 
	 */
	public void clear(){
		StopWatch watch = new StopWatch();
		watch.start();
		
		Collection<FactHandle> wmObjects = kSession.getFactHandles();
		
		// Clear objects from working memory one by one 
		for(FactHandle handle : wmObjects){
			kSession.delete(handle);
		}
	
		watch.stop();
		logger.debug("Time to clear Qualification Session is: " + watch.getTime());
	}
	
	/**
	 * Converts each ShiftAssignment to a ShiftQualificationDto object
	 * and sets its accepted flag as well as any constraints if present
	 * 
	 * @param shiftResults
	 * @return
	 */
	protected Collection<ShiftQualificationDto> convertQualificationResults(Map<ShiftAssignment, List<ShiftConstraintDto>> shiftResults){
		Collection<ShiftQualificationDto> shiftQualficationResults = new ArrayList<>();
		for(ShiftAssignment shift : shiftResults.keySet()){
			ShiftQualificationDto shiftDto = convertQualificationResultObj(shift, shiftResults.get(shift));
			// Only include reject results if include details flag is true
			if(shiftDto.getIsAccepted() || (!shiftDto.getIsAccepted() && includeResultDetails))
				shiftQualficationResults.add(shiftDto);
		}
		return shiftQualficationResults;
	}
	
	/**
	 * Convert a shift assignment to a shift constraint dto object
	 * If any constraint violations are present than shift acceptance is set to false
	 * 
	 * @param shift
	 * @param shiftResults
	 * @return
	 */
	protected ShiftQualificationDto convertQualificationResultObj(ShiftAssignment shift, List<ShiftConstraintDto> shiftResults){
		ShiftQualificationDto shiftDto = new ShiftQualificationDto(shift);
		if(shiftResults.size() > 0){
			shiftDto.setIsAccepted(false);
			if(includeResultDetails) shiftDto.setCauses(shiftResults);
		}
		return shiftDto;
	}
	
	protected void notifyProgress(double percentComplete, String message){
		if(notificationService != null){
			notificationService.notifyProgress(percentComplete, message);
		}
	}
	
}
