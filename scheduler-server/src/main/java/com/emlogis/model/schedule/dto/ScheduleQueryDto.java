package com.emlogis.model.schedule.dto;

import com.emlogis.model.schedule.ScheduleStatus;
import com.emlogis.model.schedule.ScheduleType;
import com.emlogis.model.schedule.TaskState;

import java.util.Collection;

public class ScheduleQueryDto {

    private Collection<String> sites;
    private Collection<String> teams;
    private Collection<ScheduleStatus> statuses;
    private Collection<TaskState> states;
    private ScheduleType scheduleType;
    private int scheduleLengthInDays;
    private String search;
    private long startDate;
    private ScheduleDate scheduleDate = ScheduleDate.START;
    private Paging paging;
    private Ordering ordering;

    public static enum ScheduleDate {
        START, HAS
    }

    public static class Paging {
        private int offset;
        private int limit;

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

    public static class Ordering {
        private String orderby;
        private String orderdir;

        public String getOrderby() {
            return orderby;
        }

        public void setOrderby(String orderby) {
            this.orderby = orderby;
        }

        public String getOrderdir() {
            return orderdir;
        }

        public void setOrderdir(String orderdir) {
            this.orderdir = orderdir;
        }
    }

    public Collection<String> getSites() {
        return sites;
    }

    public void setSites(Collection<String> sites) {
        this.sites = sites;
    }

    public Collection<String> getTeams() {
        return teams;
    }

    public void setTeams(Collection<String> teams) {
        this.teams = teams;
    }

    public Collection<ScheduleStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(Collection<ScheduleStatus> statuses) {
        this.statuses = statuses;
    }

    public Collection<TaskState> getStates() {
        return states;
    }

    public void setStates(Collection<TaskState> states) {
        this.states = states;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public int getScheduleLengthInDays() {
        return scheduleLengthInDays;
    }

    public void setScheduleLengthInDays(int scheduleLengthInDays) {
        this.scheduleLengthInDays = scheduleLengthInDays;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public Ordering getOrdering() {
        return ordering;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering = ordering;
    }

    public ScheduleDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(ScheduleDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }
}
