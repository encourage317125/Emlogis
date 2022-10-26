package com.emlogis.model.schedule.dto;

import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.TaskState;

import java.util.Collection;

public class ScheduleWithSiteAndTeamsDto {

    private String id;
    private String name;
    private String description;
    private long startDate;
    private long endDate;
    private int lengthInDays;
    private ScheduleStatus status;
    private TaskState state;
    private SiteDto site;
    private Collection<TeamDto> teams;

    public static class SiteDto {
        private String siteId;
        private String siteName;
        private String siteTimeZone;

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getSiteTimeZone() {
            return siteTimeZone;
        }

        public void setSiteTimeZone(String siteTimeZone) {
            this.siteTimeZone = siteTimeZone;
        }
    }

    public static class TeamDto {
        private String teamId;
        private String teamName;

        public String getTeamId() {
            return teamId;
        }

        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getLengthInDays() {
        return lengthInDays;
    }

    public void setLengthInDays(int lengthInDays) {
        this.lengthInDays = lengthInDays;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public SiteDto getSite() {
        return site;
    }

    public void setSite(SiteDto site) {
        this.site = site;
    }

    public Collection<TeamDto> getTeams() {
        return teams;
    }

    public void setTeams(Collection<TeamDto> teams) {
        this.teams = teams;
    }
}
