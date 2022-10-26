package com.emlogis.model.schedule.dto;

import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.dto.Dto;

import java.util.Collection;
import java.util.Map;

public class PostAllOpenShiftDto extends Dto {

    private Collection<String> openShifts;
    private long deadline;
    private String comments;
    private String terms;
    private Map<ConstraintOverrideType, Boolean> overrideOptions;
    private QualificationExecuteDto executeDto;

    public Collection<String> getOpenShifts() {
        return openShifts;
    }

    public void setOpenShifts(Collection<String> openShifts) {
        this.openShifts = openShifts;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public Map<ConstraintOverrideType, Boolean> getOverrideOptions() {
        return overrideOptions;
    }

    public void setOverrideOptions(Map<ConstraintOverrideType, Boolean> overrideOptions) {
        this.overrideOptions = overrideOptions;
    }

    public QualificationExecuteDto getExecuteDto() {
        return executeDto;
    }

    public void setExecuteDto(QualificationExecuteDto executeDto) {
        this.executeDto = executeDto;
    }
}
