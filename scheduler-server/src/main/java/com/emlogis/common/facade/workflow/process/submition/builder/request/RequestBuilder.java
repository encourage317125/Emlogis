package com.emlogis.common.facade.workflow.process.submition.builder.request;

import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.submit.SubmitDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.entities.WflProcess;

/**
 * Created by user on 20.08.15.
 */
public interface RequestBuilder<REQUEST_TYPE extends SubmitDto> {

    /**
     *
     * @param requestDto
     * @param parent
     * @param userAccount
     * @return
     */
    SubmitRequestResultDto build(REQUEST_TYPE requestDto, WflProcess parent, UserAccount userAccount);
}
