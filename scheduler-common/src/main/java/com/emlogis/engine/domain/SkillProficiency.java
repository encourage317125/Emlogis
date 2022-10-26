package com.emlogis.engine.domain;

public class SkillProficiency  {

    private Employee employee;
    private Skill skill;
    
    private int skillLevel;
    
    private boolean isPrimarySkill;

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

	public int getSkillLevel() {
		return skillLevel;
	}
	
	public String getSkillId(){
		return skill.getCode();
	}

	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}

	public boolean isPrimarySkill() {
		return isPrimarySkill;
	}

	public void setPrimarySkill(boolean isPrimarySkill) {
		this.isPrimarySkill = isPrimarySkill;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((employee == null) ? 0 : employee.hashCode());
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
		SkillProficiency other = (SkillProficiency) obj;
		if (employee == null) {
			if (other.employee != null)
				return false;
		} else if (!employee.equals(other.employee))
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
		builder.append("SkillProficiency [employee=");
		builder.append(employee);
		builder.append(", skill=");
		builder.append(skill);
		builder.append(", skillLevel=");
		builder.append(skillLevel);
		builder.append(", isPrimarySkill=");
		builder.append(isPrimarySkill);
		builder.append("]");
		return builder.toString();
	}

}
