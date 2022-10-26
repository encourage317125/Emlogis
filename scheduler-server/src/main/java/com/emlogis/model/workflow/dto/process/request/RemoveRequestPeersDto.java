package com.emlogis.model.workflow.dto.process.request;

import com.emlogis.model.workflow.dto.process.response.ProcessRecipientDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 17.04.15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveRequestPeersDto implements Serializable {

    @JsonProperty(value = "peers", required = true)
    private List<ProcessRecipientDto> peers;

    public RemoveRequestPeersDto() {
    }

    public List<ProcessRecipientDto> getPeers() {
        if (peers == null) {
            peers = new ArrayList<>();
        }
        return peers;
    }

    public void setPeers(List<ProcessRecipientDto> peers) {
        this.peers = peers;
    }

}
