package com.emlogis.model.tenant.dto;

import com.emlogis.model.dto.UpdateDto;
import com.emlogis.model.tenant.UnsucessfullLoginAction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PasswordPoliciesUpdateDto extends UpdateDto {
		
	private boolean forceChangeOnFirstLogon;

	private int	minPasswordLength;
	private int	maxPasswordLength;	
	private int	disallowOldPasswordNb;
	
	private	int maxUnsucessfullLogin;
	private UnsucessfullLoginAction unsucessfullLoginAction;
	private	int suspendAccountTime;
	
	private boolean requireAtLeastOneUppercaseChar;
	private boolean requireAtLeastOneLowercaseChar;
	private boolean requireAtLeastOneNumberChar;	
	private boolean requireAtLeastOneNonalphaChar;	
	
	public PasswordPoliciesUpdateDto() {}

	public boolean isForceChangeOnFirstLogon() {
		return forceChangeOnFirstLogon;
	}

	public void setForceChangeOnFirstLogon(boolean forceChangeOnFirstLogon) {
		this.forceChangeOnFirstLogon = forceChangeOnFirstLogon;
	}

	public int getMinPasswordLength() {
		return minPasswordLength;
	}

	public void setMinPasswordLength(int minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

	public int getMaxPasswordLength() {
		return maxPasswordLength;
	}

	public void setMaxPasswordLength(int maxPasswordLength) {
		this.maxPasswordLength = maxPasswordLength;
	}

	public int getDisallowOldPasswordNb() {
		return disallowOldPasswordNb;
	}

	public void setDisallowOldPasswordNb(int disallowOldPasswordNb) {
		this.disallowOldPasswordNb = disallowOldPasswordNb;
	}

	public int getMaxUnsucessfullLogin() {
		return maxUnsucessfullLogin;
	}

	public void setMaxUnsucessfullLogin(int maxUnsucessfullLogin) {
		this.maxUnsucessfullLogin = maxUnsucessfullLogin;
	}

	public UnsucessfullLoginAction getUnsucessfullLoginAction() {
		return unsucessfullLoginAction;
	}

	public void setUnsucessfullLoginAction(
			UnsucessfullLoginAction unsucessfullLoginAction) {
		this.unsucessfullLoginAction = unsucessfullLoginAction;
	}

	public int getSuspendAccountTime() {
		return suspendAccountTime;
	}

	public void setSuspendAccountTime(int suspendAccountTime) {
		this.suspendAccountTime = suspendAccountTime;
	}

	public boolean isRequireAtLeastOneUppercaseChar() {
		return requireAtLeastOneUppercaseChar;
	}

	public void setRequireAtLeastOneUppercaseChar(
			boolean requireAtLeastOneUppercaseChar) {
		this.requireAtLeastOneUppercaseChar = requireAtLeastOneUppercaseChar;
	}

	public boolean isRequireAtLeastOneLowercaseChar() {
		return requireAtLeastOneLowercaseChar;
	}

	public void setRequireAtLeastOneLowercaseChar(
			boolean requireAtLeastOneLowercaseChar) {
		this.requireAtLeastOneLowercaseChar = requireAtLeastOneLowercaseChar;
	}

	public boolean isRequireAtLeastOneNumberChar() {
		return requireAtLeastOneNumberChar;
	}

	public void setRequireAtLeastOneNumberChar(boolean requireAtLeastOneNumberChar) {
		this.requireAtLeastOneNumberChar = requireAtLeastOneNumberChar;
	}

	public boolean isRequireAtLeastOneNonalphaChar() {
		return requireAtLeastOneNonalphaChar;
	}

	public void setRequireAtLeastOneNonalphaChar(
			boolean requireAtLeastOneNonalphaChar) {
		this.requireAtLeastOneNonalphaChar = requireAtLeastOneNonalphaChar;
	}

}
