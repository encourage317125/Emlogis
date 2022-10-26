package com.emlogis.model.employee.dto;

import com.emlogis.model.tenant.dto.UserAccountDto;

import java.util.Collection;
import java.util.Map;

public class EmployeeManagerViewDto extends EmployeeDto {

    private UserAccountDto userAccountDto;
    private NotificationSettingDto notificationSettings;
    private AutoApprovalsSettingDto autoApprovalsSettingDto;
    private Map<String, Object> siteInfo;
    private Collection<Map<String, Object>> roleInfo;
    private Collection<Map<String, Object>> skillInfo;
    private Collection<Map<String, Object>> teamInfo;
    private Collection<Map<String, Object>> ptoInfo;
    private Collection<String[]> licenseCertificationInfo;

    public UserAccountDto getUserAccountDto() {
        return userAccountDto;
    }

    public void setUserAccountDto(UserAccountDto userAccountDto) {
        this.userAccountDto = userAccountDto;
    }

    public NotificationSettingDto getNotificationSettings() {
		return notificationSettings;
	}

	public void setNotificationSettings(NotificationSettingDto notificationSettings) {
		this.notificationSettings = notificationSettings;
	}

	public AutoApprovalsSettingDto getAutoApprovalsSettingDto() {
        return autoApprovalsSettingDto;
    }

    public void setAutoApprovalsSettingDto(AutoApprovalsSettingDto autoApprovalsSettingDto) {
        this.autoApprovalsSettingDto = autoApprovalsSettingDto;
    }

    public Map<String, Object> getSiteInfo() {
        return siteInfo;
    }

    public void setSiteInfo(Map<String, Object> siteInfo) {
        this.siteInfo = siteInfo;
    }

    public Collection<Map<String, Object>> getRoleInfo() {
        return roleInfo;
    }

    public void setRoleInfo(Collection<Map<String, Object>> roleInfo) {
        this.roleInfo = roleInfo;
    }

    public Collection<Map<String, Object>> getSkillInfo() {
        return skillInfo;
    }

    public void setSkillInfo(Collection<Map<String, Object>> skillInfo) {
        this.skillInfo = skillInfo;
    }

    public Collection<Map<String, Object>> getTeamInfo() {
        return teamInfo;
    }

    public void setTeamInfo(Collection<Map<String, Object>> teamInfo) {
        this.teamInfo = teamInfo;
    }

    public Collection<Map<String, Object>> getPtoInfo() {
        return ptoInfo;
    }

    public void setPtoInfo(Collection<Map<String, Object>> ptoInfo) {
        this.ptoInfo = ptoInfo;
    }

    public Collection<String[]> getLicenseCertificationInfo() {
        return licenseCertificationInfo;
    }

    public void setLicenseCertificationInfo(Collection<String[]> licenseCertificationInfo) {
        this.licenseCertificationInfo = licenseCertificationInfo;
    }
}
