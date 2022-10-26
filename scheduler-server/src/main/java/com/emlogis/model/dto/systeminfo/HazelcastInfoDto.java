package com.emlogis.model.dto.systeminfo;

import com.emlogis.model.dto.Dto;

public class HazelcastInfoDto extends Dto{

    int globalAssignmentReqQueueSize;
    int globalQualificationReqQueueSize;
    int globalResponseQueueSize;
    int requestDataMapSize;
    int responseDataMapSize;
    int qualificationTrackerMapSize;
    int abortMapSize;
    int shutdownMapSize;
    int appServers;
    int engines;

    public int getGlobalAssignmentReqQueueSize() {
        return globalAssignmentReqQueueSize;
    }

    public void setGlobalAssignmentReqQueueSize(int globalAssignmentReqQueueSize) {
        this.globalAssignmentReqQueueSize = globalAssignmentReqQueueSize;
    }

    public int getGlobalQualificationReqQueueSize() {
        return globalQualificationReqQueueSize;
    }

    public void setGlobalQualificationReqQueueSize(int globalQualificationReqQueueSize) {
        this.globalQualificationReqQueueSize = globalQualificationReqQueueSize;
    }

    public int getGlobalResponseQueueSize() {
        return globalResponseQueueSize;
    }

    public void setGlobalResponseQueueSize(int globalResponseQueueSize) {
        this.globalResponseQueueSize = globalResponseQueueSize;
    }

    public int getRequestDataMapSize() {
        return requestDataMapSize;
    }

    public void setRequestDataMapSize(int requestDataMapSize) {
        this.requestDataMapSize = requestDataMapSize;
    }

    public int getResponseDataMapSize() {
        return responseDataMapSize;
    }

    public void setResponseDataMapSize(int responseDataMapSize) {
        this.responseDataMapSize = responseDataMapSize;
    }

    public int getQualificationTrackerMapSize() {
        return qualificationTrackerMapSize;
    }

    public void setQualificationTrackerMapSize(int qualificationTrackerMapSize) {
        this.qualificationTrackerMapSize = qualificationTrackerMapSize;
    }

    public int getAbortMapSize() {
        return abortMapSize;
    }

    public void setAbortMapSize(int abortMapSize) {
        this.abortMapSize = abortMapSize;
    }

    public int getShutdownMapSize() {
        return shutdownMapSize;
    }

    public void setShutdownMapSize(int shutdownMapSize) {
        this.shutdownMapSize = shutdownMapSize;
    }

    public int getAppServers() {
        return appServers;
    }

    public void setAppServers(int appServers) {
        this.appServers = appServers;
    }

    public int getEngines() {
        return engines;
    }

    public void setEngines(int engines) {
        this.engines = engines;
    }
}
