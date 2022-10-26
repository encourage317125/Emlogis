package com.emlogis.model.workflow.dto.process.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexborlis on 08.02.15.
 */
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateWorkflowProcessResponseDto implements Serializable {

  //  @XmlElement
    List<WflOriginatorInstanceBriefInfoDto> instances;


    public CreateWorkflowProcessResponseDto() {
    }

    public List<WflOriginatorInstanceBriefInfoDto> getInstances() {
        if (instances == null) {
            instances = new ArrayList<>();
        }
        return instances;
    }

    public void setInstances(List<WflOriginatorInstanceBriefInfoDto> instances) {
        if (instances == null) {
            instances = new ArrayList<>();
        }
        this.instances = instances;
    }

}
