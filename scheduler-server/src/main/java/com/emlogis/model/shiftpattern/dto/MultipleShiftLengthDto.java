package com.emlogis.model.shiftpattern.dto;

import java.util.Collection;

public class MultipleShiftLengthDto {

    private Collection<CreateShiftLengthDto> shiftLengthDtos;

    public static class CreateShiftLengthDto {
        private Integer lengthInMin;
        private boolean active = true;

        public Integer getLengthInMin() {
            return lengthInMin;
        }

        public void setLengthInMin(Integer lengthInMin) {
            this.lengthInMin = lengthInMin;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    public Collection<CreateShiftLengthDto> getShiftLengthDtos() {
        return shiftLengthDtos;
    }

    public void setShiftLengthDtos(Collection<CreateShiftLengthDto> shiftLengthDtos) {
        this.shiftLengthDtos = shiftLengthDtos;
    }
}
