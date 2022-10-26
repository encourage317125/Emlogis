package com.emlogis.engine.domain;

import java.io.Serializable;

public class ShiftSkillRequirement implements Serializable {

	private Shift shift;
	private Skill skill;

	public ShiftSkillRequirement() {}

	public ShiftSkillRequirement(Shift shift, Skill skill) {
		this.shift = shift;
		this.skill = skill;
	}

	public Shift getShift() {
		return shift;
	}
	
	public String getShiftId() {
		return shift.getId();
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((shift == null) ? 0 : shift.hashCode());
		result = prime * result + ((skill == null) ? 0 : skill.hashCode());
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
		ShiftSkillRequirement other = (ShiftSkillRequirement) obj;
		if (shift == null) {
			if (other.shift != null)
				return false;
		} else if (!shift.equals(other.shift))
			return false;
		if (skill == null) {
			if (other.skill != null)
				return false;
		} else if (!skill.equals(other.skill))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShiftSkillRequirement [shift=");
		builder.append(shift);
		builder.append(", skill=");
		builder.append(skill);
		builder.append("]");
		return builder.toString();
	}

}
