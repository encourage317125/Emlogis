package com.emlogis.model.dto.convenience;

import com.emlogis.model.AccessType;

import java.io.Serializable;
import java.util.Set;

public class ACEConfigurationAllSitesDto implements Serializable {

    private AccessType allSitesAccessType;
    private Set<ACEConfigurationSiteDto> result;

    public AccessType getAllSitesAccessType() {
        return allSitesAccessType;
    }

    public void setAllSitesAccessType(AccessType allSitesAccessType) {
        this.allSitesAccessType = allSitesAccessType;
    }

    public Set<ACEConfigurationSiteDto> getResult() {
        return result;
    }

    public void setResult(Set<ACEConfigurationSiteDto> result) {
        this.result = result;
    }
}
