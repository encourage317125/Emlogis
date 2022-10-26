package com.emlogis.model.dto;

public abstract class CreateDto<T extends UpdateDto> extends Dto {

    public final static String UPDATE_DTO = "updateDto";

    private String id;
    private T updateDto;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getUpdateDto() {
        return updateDto;
    }

    public void setUpdateDto(T updateDto) {
        this.updateDto = updateDto;
    }
}
