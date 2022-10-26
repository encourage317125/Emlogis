package com.emlogis.model.workflow.dto.process.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucas on 25.05.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WflOriginatorProcessInstanceDetailedDto extends WflOriginatorInstanceBriefInfoDto implements Serializable {

    private List<WflOriginatorPeerInfoDto> peers;

    public WflOriginatorProcessInstanceDetailedDto(WflOriginatorInstanceBriefInfoDto parentDto) {
        super(parentDto);
    }

    public List<WflOriginatorPeerInfoDto> getPeers() {
        if (peers == null) {
            peers = new ArrayList<>();
        }
        return peers;
    }

    public void setPeers(List<WflOriginatorPeerInfoDto> peers) {
        this.peers = peers;
    }
}
