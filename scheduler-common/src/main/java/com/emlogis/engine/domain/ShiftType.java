/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emlogis.engine.domain;

import org.joda.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;


public class ShiftType implements Serializable {

    private String shiftId;
    private boolean isExcessShift; 
    private LocalTime startTime;
    private LocalTime endTime; 
    private String description;

    @JsonIgnore
    public String getStartTimeString() {
        return startTime.toString();
    }

    @JsonIgnore
    public String getEndTimeString() {
        return endTime.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public boolean isExcessShift() {
		return isExcessShift;
	}

	public void setExcessShift(boolean isExcessShift) {
		this.isExcessShift = isExcessShift;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShiftType [shiftId=");
		builder.append(shiftId);
		builder.append(", isExcessShift=");
		builder.append(isExcessShift);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((shiftId == null) ? 0 : shiftId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShiftType other = (ShiftType) obj;
		if (shiftId == null) {
			if (other.shiftId != null)
				return false;
		} else if (!shiftId.equals(other.shiftId))
			return false;
		return true;
	}


}
