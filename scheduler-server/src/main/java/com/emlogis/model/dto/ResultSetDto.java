package com.emlogis.model.dto;

import java.util.Collection;

public class ResultSetDto<T> extends ReadDto {

    private Collection<T> result;
    private int total;

    public Collection<T> getResult() {
        return result;
    }

    public ResultSetDto<T> setResult(Collection<T> result) {
        this.result = result;
        return this;
    }

    public int getTotal() {
        return total;
    }

    public ResultSetDto<T> setTotal(int total) {
        this.total = total;
        return this;
    }

    public String getClName() {						// used and required by toDto conversion
        return this.getClass().getSimpleName();
    }

    public void setClName(String cName) {}
}
