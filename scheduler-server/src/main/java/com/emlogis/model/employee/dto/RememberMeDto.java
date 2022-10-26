package com.emlogis.model.employee.dto;

import com.emlogis.model.dto.BaseEntityDto;

public class RememberMeDto extends BaseEntityDto {

    private	String clientUniqueId;
    private	String clientDescr;
    private	String tokenId;
    private long expirationDate;

    public String getClientUniqueId() {
        return clientUniqueId;
    }

    public void setClientUniqueId(String clientUniqueId) {
        this.clientUniqueId = clientUniqueId;
    }

    public String getClientDescr() {
        return clientDescr;
    }

    public void setClientDescr(String clientDescr) {
        this.clientDescr = clientDescr;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }
}
