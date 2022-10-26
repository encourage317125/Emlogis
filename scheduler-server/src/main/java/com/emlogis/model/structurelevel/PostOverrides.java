package com.emlogis.model.structurelevel;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class PostOverrides extends BaseEntity {

    private String 	name;

    @Column(length=1024)
    private String	overrideOptions;		// This is json string capturing a Map of (Constraint) OverrideOptions
											// where k = ConstraintOverrideType and v = boolean (true/false)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "siteTenantId", referencedColumnName = "tenantId"),
            @JoinColumn(name = "siteId", referencedColumnName = "id")
    })
    private Site site;
    
    public PostOverrides() {}

    public PostOverrides(PrimaryKey primaryKey) {
        super(primaryKey);
        setOverrideOptions(getDefaultOverrides());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public static Map<ConstraintOverrideType, Boolean> getDefaultOverrides() {
        Map<ConstraintOverrideType, Boolean> map = new HashMap<>();
        for (ConstraintOverrideType ot : ConstraintOverrideType.values()) {
    		map.put(ot, false);        	
        }	
    	return map; 	
    }

    @SuppressWarnings("unchecked")
	public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
		if (overrideOptions == null) {
			return getDefaultOverrides();
        } else {
            return EmlogisUtils.fromJsonString(overrideOptions,
                    new TypeReference<Map<ConstraintOverrideType, Boolean>>() {});
		}
	}

	public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
		this.overrideOptions = EmlogisUtils.toJsonString(overrideOptions);
	}
 
    
}
