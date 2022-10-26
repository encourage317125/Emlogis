package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.CreateDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamEmployeeCreateDto extends CreateDto implements Serializable {

    private Boolean isFloating;

    private Boolean isHomeTeam;

    private Boolean isSchedulable;

    private String employeeId;

    /**
     * @return the isFloating
     */
    public Boolean getIsFloating() {
        return isFloating;
    }

    /**
     * @param isFloating the isFloating to set
     */
    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }

    /**
     * @return the isHomeTeam
     */
    public Boolean getIsHomeTeam() {
        return isHomeTeam;
    }

    /**
     * @param isHomeTeam the isHomeTeam to set
     */
    public void setIsHomeTeam(Boolean isHomeTeam) {
        this.isHomeTeam = isHomeTeam;
    }

    /**
     * @return the isSchedulable
     */
    public Boolean getIsSchedulable() {
        return isSchedulable;
    }

    /**
     * @param isSchedulable the isSchedulable to set
     */
    public void setIsSchedulable(Boolean isSchedulable) {
        this.isSchedulable = isSchedulable;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
