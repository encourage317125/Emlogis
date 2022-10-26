package com.emlogis.model.workflow.dto.commentary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15.07.15.
 */
public class RequestCommentary implements Serializable {

    private List<RequestCommentDto> commentary;

    public RequestCommentary() {
    }

    public List<RequestCommentDto> getCommentary() {
        if (commentary == null) {
            return new ArrayList<>();
        }
        return commentary;
    }

    public void setCommentary(List<RequestCommentDto> commentary) {
        this.commentary = commentary;
    }
}
