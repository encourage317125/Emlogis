package com.emlogis.engine.domain.contract.contractline.dto;

import com.emlogis.engine.domain.contract.contractline.ContractLineType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @Type(value = IntMinMaxCLDto.class, name = "IntMinMaxCLDto"),
        @Type(value = WeekdayRotationPatternCLDto.class, name = "WeekdayRotationPatternCLDto"),
        @Type(value = WeekendWorkPatternCLDto.class, name = "WeekendWorkPatternCLDto"),
        @Type(value = BooleanCLDto.class, name = "BooleanCLDto")})
public abstract class ContractLineDto implements Serializable {

//    private String id;
//    private String category;
    private String name;
    private ContractLineType contractLineType;
    private String contractId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContractLineType getContractLineType() {
        return contractLineType;
    }

    public void setContractLineType(ContractLineType contractLineType) {
        this.contractLineType = contractLineType;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }
}
