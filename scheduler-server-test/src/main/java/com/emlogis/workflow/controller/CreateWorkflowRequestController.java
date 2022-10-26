package com.emlogis.workflow.controller;

import com.emlogis.workflow.controller.dto.WIPCreateReportDto;
import com.emlogis.workflow.controller.dto.WipCreateRequestDto;
import com.emlogis.workflow.service.CreateRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by user on 01.10.15.
 */
@Controller
public class CreateWorkflowRequestController {


    @Autowired
    private CreateRequestService createRequestService;


    @RequestMapping(value = "/wip/create", method = RequestMethod.POST)
    @ResponseBody
    public WIPCreateReportDto calculate(
            WipCreateRequestDto requestDto
    ) {
        return createRequestService.createWip(requestDto);
    }

}
