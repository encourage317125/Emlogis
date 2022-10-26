package com.emlogis.model.tenant;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity()
public class PasswordPolicies extends BaseEntity implements Serializable {

	// policies
	private int forceChangeEveryNDays = -1;				// <= 0 = disabled, otherwise value = max nb of days between password changes
	private boolean forceChangeOnFirstLogon = false;

	private int disallowOldPasswordNb = 0;				// 0 = disabled
	
	private	int maxUnsucessfullLogin;					// 0 = disabled
	private UnsucessfullLoginAction unsucessfullLoginAction = UnsucessfullLoginAction.NOACTION;
	private	int suspendAccountTime = 30;				// in minutes

	// password strength criteria
	private int	minPasswordLength = 4;					// -1 = disabled
	private int	maxPasswordLength = -1;					// -1 = disabled
	private boolean requireAtLeastOneUppercaseChar = false;
	private boolean requireAtLeastOneLowercaseChar = false;
	private boolean requireAtLeastOneNumberChar = false;	
	private boolean requireAtLeastOneNonalphaChar = false;	
	
	public PasswordPolicies() {}

	public PasswordPolicies(PrimaryKey primaryKey) {
		super(primaryKey);
	}

    public int getForceChangeEveryNDays() {
		return forceChangeEveryNDays;
	}

	public void setForceChangeEveryNDays(int forceChangeEveryNDays) {
		this.forceChangeEveryNDays = forceChangeEveryNDays;
	}

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
