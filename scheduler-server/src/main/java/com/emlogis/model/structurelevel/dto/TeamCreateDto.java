package com.emlogis.model.structurelevel.dto;


import com.emlogis.model.dto.CreateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamCreateDto extends CreateDto<TeamUpdateDto> implements Serializable {

    public final static String SITE_ID = "siteId";
    public final static String NAME = UPDATE_DTO + ".name";
    public final static String DESCRIPTION = UPDATE_DTO + ".description";
    public final static String ABBREVIATION = UPDATE_DTO + ".abbreviation";

	private String siteId;

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

}