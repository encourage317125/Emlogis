package com.emlogis.model.schedule;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.tenant.settings.scheduling.OptimPreferenceOptimizationSetting;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(name = "SchedulingOptions")
public class SchedulingOptions extends BaseEntity implements Cloneable {

    @Column()
    private String optimizationPreferenceSetting = new OptimPreferenceOptimizationSetting().getValue();    	
    											// this field is for specifying which constraint should be privileged
    											// while doing the optimization. overrides the Organization level setting 
    											// only if overrideOptimizationPreference = true
    
    
	private boolean	overrideOptimizationPreference = false;	// flag indicating if optimizationPreferenceSetting overrides 
												// org level optimization settings

    @Column()
    @Lob
    private String overrideOptions;		// This is json string capturing a Map of (Constraint) OverrideOptions
    									// where k = ConstraintOverrideType and v = OverrideOption
    									// on SchedulingOptions creation, a map of OverrideOptions must be built
    									// with an entry for each ConstraintOverrideType and a deafult OverrideOption object

    public SchedulingOptions() {}

    public SchedulingOptions(PrimaryKey primaryKey) {
        super(primaryKey);
    }

    public static Map<ConstraintOverrideType, OverrideOption> getDefaultOverrides() {
    	
        Map<ConstraintOverrideType, OverrideOption> map = new HashMap<>();
        for (ConstraintOverrideType ot : ConstraintOverrideType.values()) {
    		map.put(ot, new OverrideOption());
        }	
    	return map; 	
    }

    @SuppressWarnings("unchecked")
	public Map<ConstraintOverrideType, OverrideOption> getOverrideOptions() {
		if (overrideOptions == null) {
			return getDefaultOverrides();
        } else {
            return EmlogisUtils.fromJsonString(overrideOptions,
                    new TypeReference<Map<ConstraintOverrideType, OverrideOption>>() {});
		}
	}

	public void setOverrideOptions(Map<ConstraintOverrideType, OverrideOption> overrideOptions) {
		this.overrideOptions = EmlogisUtils.toJsonString(overrideOptions);
	}

	
    public String getOptimizationPreferenceSetting() {
		return optimizationPreferenceSetting;
	}

	public void setOptimizationPreferenceSetting(
			String optimizationPreferenceSetting) {
		this.optimizationPreferenceSetting = optimizationPreferenceSetting;
	}

	public boolean isOverrideOptimizationPreference() {
		return overrideOptimizationPreference;
	}

	public void setOverrideOptimizationPreference(
			boolean overrideOptimizationPreference) {
		this.overrideOptimizationPreference = overrideOptimizationPreference;
	}

	@Override
    public SchedulingOptions clone() throws CloneNotSupportedException {
        return (SchedulingOptions) super.clone();
    }
}

