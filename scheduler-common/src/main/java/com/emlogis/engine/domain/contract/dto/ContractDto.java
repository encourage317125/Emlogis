package com.emlogis.engine.domain.contract.dto;

import com.emlogis.engine.domain.contract.contractline.ContractScope;
import com.emlogis.engine.domain.contract.contractline.dto.ContractLineDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContractDto implements Serializable {

    private String id;
    private ContractScope scope;
    private List<ContractLineDto> contractLineDtos;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ContractScope getScope() {
        return scope;
    }

    public void setScope(ContractScope scope) {
        this.scope = scope;
    }

    public List<ContractLineDto> getContractLineDtos() {
        if (contractLineDtos == null) {
            contractLineDtos = new ArrayList<>();
        }
        return contractLineDtos;
    }

    public void setContractLineDtos(List<ContractLineDto> contractLineDtos) {
        this.contractLineDtos = contractLineDtos;
    }
}
