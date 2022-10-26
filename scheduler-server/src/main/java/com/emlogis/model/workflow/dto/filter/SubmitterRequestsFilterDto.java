package com.emlogis.model.workflow.dto.filter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by lucas on 27.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitterRequestsFilterDto extends RequestFilterDto implements Serializable {

    public SubmitterRequestsFilterDto() {
    }

}
