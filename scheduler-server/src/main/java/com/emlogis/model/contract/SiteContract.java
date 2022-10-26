package com.emlogis.model.contract;

import com.emlogis.model.PrimaryKey;
import com.emlogis.model.structurelevel.Site;

import javax.persistence.*;

@Entity
public class SiteContract extends Contract {
	
    //bi-directional many-to-one association to Site
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "siteTenantId", referencedColumnName = "tenantId"),
        @JoinColumn(name = "siteId", referencedColumnName = "id")
    })
    private Site site;

	public SiteContract() {
		super();
	}

	public SiteContract(PrimaryKey primaryKey) {
		super(primaryKey);
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
    }

	public String getSiteId() {
		return site.getId();
	}
}
