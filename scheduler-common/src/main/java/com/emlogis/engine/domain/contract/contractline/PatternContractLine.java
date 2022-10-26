package com.emlogis.engine.domain.contract.contractline;

import com.emlogis.engine.domain.contract.patterns.Pattern;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class PatternContractLine extends ContractLine {

	private Pattern pattern;

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return pattern != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatternContractLine other = (PatternContractLine) obj;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		return true;
	}

}
