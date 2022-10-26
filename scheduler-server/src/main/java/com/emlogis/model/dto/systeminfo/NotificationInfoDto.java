package com.emlogis.model.dto.systeminfo;

import com.emlogis.model.dto.Dto;

public class NotificationInfoDto extends Dto {

    int send;
    int receive;
    int archivedReceiveQueue;
    int archivedSendQueue;

    public int getSend() {
        return send;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public int getReceive() {
        return receive;
    }

    public void setReceive(int receive) {
        this.receive = receive;
    }

    public int getArchivedReceiveQueue() {
        return archivedReceiveQueue;
    }

    public void setArchivedReceiveQueue(int archivedReceiveQueue) {
        this.archivedReceiveQueue = archivedReceiveQueue;
    }

    public int getArchivedSendQueue() {
        return archivedSendQueue;
    }

    public void setArchivedSendQueue(int archivedSendQueue) {
        this.archivedSendQueue = archivedSendQueue;
    }
}
