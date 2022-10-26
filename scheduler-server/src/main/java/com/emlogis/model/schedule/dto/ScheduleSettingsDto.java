package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.engine.domain.solver.RuleName;
import com.emlogis.model.dto.Dto;
import com.emlogis.model.schedule.OverrideOption;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleSettingsDto extends Dto implements Serializable {
	
	private boolean	preservePreAssignedShifts;
    private boolean	preservePostAssignedShifts;
    private boolean	preserveEngineAssignedShifts;

    private int maxComputationTime;
    private int maximumUnimprovedSecondsSpent;

    private Map<RuleName, Integer> ruleWeightMultipliers; 	

    private Map<ConstraintOverrideType, OverrideOption> overrideOptions;

    private String optimizationPreferenceSetting;
    private String siteOptimizationPreferenceSetting;
    private boolean overrideOptimizationPreference;

    private Map<String, Integer> additionInfo;

    private Object[] employees;

    public boolean isPreservePreAssignedShifts() {
        return preservePreAssignedShifts;
    }

    public void setPreservePreAssignedShifts(boolean preservePreAssignedShifts) {
        this.preservePreAssignedShifts = preservePreAssignedShifts;
    }

    public boolean isPreservePostAssignedShifts() {
        return preservePostAssignedShifts;
    }

    public void setPreservePostAssignedShifts(boolean preservePostAssignedShifts) {
        this.preservePostAssignedShifts = preservePostAssignedShifts;
    }

    public boolean isPreserveEngineAssignedShifts() {
        return preserveEngineAssignedShifts;
    }

    public void setPreserveEngineAssignedShifts(boolean preserveEngineAssignedShifts) {
        this.preserveEngineAssignedShifts = preserveEngineAssignedShifts;
    }

    public int getMaxComputationTime() {
        return maxComputationTime;
    }

    public void setMaxComputationTime(int maxComputationTime) {
        this.maxComputationTime = maxComputationTime;
    }

    public int getMaximumUnimprovedSecondsSpent() {
        return maximumUnimprovedSecondsSpent;
    }

    public void setMaximumUnimprovedSecondsSpent(int maximumUnimprovedSecondsSpent) {
        this.maximumUnimprovedSecondsSpent = maximumUnimprovedSecondsSpent;
    }

    public Map<RuleName, Integer> getRuleWeightMultipliers() {
        return ruleWeightMultipliers;
    }

    public void setRuleWeightMultipliers(Map<RuleName, Integer> ruleWeightMultipliers) {
        this.ruleWeightMultipliers = ruleWeightMultipliers;
    }

    public String getOptimizationPreferenceSetting() {
        return optimizationPreferenceSetting;
    }

    public void setOptimizationPreferenceSetting(String optimizationPreferenceSetting) {
        this.optimizationPreferenceSetting = optimizationPreferenceSetting;
    }

    public String getSiteOptimizationPreferenceSetting() {
        return siteOptimizationPreferenceSetting;
    }

    public void setSiteOptimizationPreferenceSetting(String siteOptimizationPreferenceSetting) {
        this.siteOptimizationPreferenceSetting = siteOptimizationPreferenceSetting;
    }

    public boolean isOverrideOptimizationPreference() {
        return overrideOptimizationPreference;
    }

    public void setOverrideOptimizationPreference(boolean overrideOptimizationPreference) {
        this.overrideOptimizationPreference = overrideOptimizationPreference;
    }

    public Map<ConstraintOverrideType, OverrideOption> getOverrideOptions() {
        return overrideOptions;
    }

    public void setOverrideOptions(Map<ConstraintOverrideType, OverrideOption> overrideOptions) {
        this.overrideOptions = overrideOptions;
    }

    public Map<String, Integer> getAdditionInfo() {
        return additionInfo;
    }

    public void setAdditionInfo(Map<String, Integer> additionInfo) {
        this.additionInfo = additionInfo;
    }

    public Object[] getEmployees() {
        return employees;
    }

    public void setEmployees(Object[] employees) {
        this.employees = employees;
    }
}
