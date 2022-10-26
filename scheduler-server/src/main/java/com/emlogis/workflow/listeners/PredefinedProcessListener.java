//package com.emlogis.workflow.listeners;
//
//import com.emlogis.common.services.workflow.process.intance.WflInstanceService;
//import com.emlogis.common.services.workflow.process.update.WflInstanceUpdateService;
//import com.emlogis.model.workflow.entities.WflProcessInstance;
//import com.emlogis.workflow.api.action.WorkflowActionFacade;
////import com.emlogis.workflow.context.WorkflowEngineContext;
//import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
//import com.emlogis.workflow.exception.WorkflowServerException;
//import org.apache.log4j.Logger;
//import org.kie.api.event.process.*;
//import org.kie.api.runtime.process.ProcessInstance;
//
//import javax.ejb.EJB;
//import javax.ejb.LocalBean;
//import javax.ejb.Stateless;
//
///**
// * Created by alexborlis on 09.02.15.
// */
//@Stateless
//@LocalBean
//public class PredefinedProcessListener implements org.kie.api.event.process.ProcessEventListener {
//
//    private final static Logger logger = Logger.getLogger(PredefinedProcessListener.class);
//
//    @EJB
//    private WflInstanceService instanceService;
//
//    @EJB
//    private WflInstanceUpdateService updateService;
//
////    @EJB
////    private WorkflowEngineContext runtime;
//
//    @EJB
//    private WorkflowActionFacade actionFacade;
//
//    @Override
//    public void beforeProcessStarted(ProcessStartedEvent event) {
//    }
//
//    @Override
//    public void afterProcessStarted(ProcessStartedEvent event) {
////        ProcessInstance processInstance = event.getProcessInstance();
////        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
////        if (wflpi != null) {
////            updateStatus(wflpi, processInstance);
////            wflpi.setStatus(RequestTechnicalStatusDict.STARTED);
////        }
////        logger.info("afterProcessStarted " + event.getProcessInstance().getProcessId());
//    }
//
//    private void updateStatus(WflProcessInstance wflpi, ProcessInstance processInstance) {
//
//    }
//
//    @Override
//    public void beforeProcessCompleted(ProcessCompletedEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//            RequestTechnicalStatusDict status = wflpi.getStatus();
//            switch (status) {
//                case READY_FOR_ACTION: {
//                    logger.info("READY_FOR_ACTION");
//                    try {
//                        updateService.actionStarted(wflpi);
//                        actionFacade.action(wflpi);
//                        break;
//                    } catch (WorkflowServerException exc) {
//                        logger.error("Error while trying to track process start", exc);
//                    }
//                }
//                case DECLINED_BY_MANAGERS:
//                    logger.info("READY_FOR_ACTION");
//                    break;
//                case DECLINED_BY_PEERS:
//                    logger.info("READY_FOR_ACTION");
//                    break;
//            }
//            logger.info("beforeProcessCompleted " + event.getProcessInstance().getProcessId());
//            logger.info(wflpi.toString());
//        }
//    }
//
//    @Override
//    public void afterProcessCompleted(ProcessCompletedEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//            updateStatus(wflpi, processInstance);
//        }
//    }
//
//    @Override
//    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//        }
//    }
//
//    @Override
//    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//        }
//    }
//
//    @Override
//    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//        }
//    }
//
//    @Override
//    public void afterNodeLeft(ProcessNodeLeftEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//        }
//    }
//
//    @Override
//    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//        }
//    }
//
//    @Override
//    public void afterVariableChanged(ProcessVariableChangedEvent event) {
//        ProcessInstance processInstance = event.getProcessInstance();
//        WflProcessInstance wflpi = instanceService.findByEngineIdIfExists(processInstance.getId());
//        if (wflpi != null) {
//        }
//    }
//}
