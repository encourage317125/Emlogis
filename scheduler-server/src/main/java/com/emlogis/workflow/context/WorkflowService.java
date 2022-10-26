//package com.emlogis.workflow.context;
//
//import com.emlogis.common.services.workflow.process.intance.WflInstanceService;
//import com.emlogis.common.services.workflow.process.update.WflInstanceUpdateService;
//import org.apache.log4j.Logger;
//
//import javax.ejb.EJB;
//import javax.ejb.LocalBean;
//import javax.ejb.Stateless;
//
///**
// * Created by user on 12.06.15.
// */
//@Stateless
//@LocalBean
//public class WorkflowService {
//
//    private final static Logger logger = Logger.getLogger(WorkflowService.class);
//
//    @EJB
//    private WorkflowEngineContext context;
//
//    @EJB
//    private WflInstanceUpdateService instanceUpdateService;
//
//    @EJB
//    private WflInstanceService instanceService;
//
////    public WflProcessInstance submitAndStart(WflProcessInstance instance, String comment) throws WorkflowServerException {
////        if (instance.getEngineId() == null) {
////            if (!instance.getStatus().equals(WorkflowProcessStatusDict.FAILED_TO_SUBMIT)) {
////                submitInstance(instance);
////            } else {
////                logger.error("Can not submit process instance " + instance.toString());
////            }
////            if (!instance.getStatus().equals(WorkflowProcessStatusDict.FAILED_TO_START)) {
////                startInstance(instance, comment);
////            } else {
////                logger.error("Can not start process instance " + instance.toString());
////            }
////            instance = instanceService.update(instance);
////        }
////        return instance;
////    }
////
////    public void startInstance(WflProcessInstance instance, String comment) throws WorkflowServerException {
////        try {
////            startProcess(instance.getEngineId());
////            proceedProcess(instance);
////            instanceUpdateService.initiatorProceed(instance, instance.getInitiator(), comment);
////        } catch (Exception error) {
////            instance.setStatus(WorkflowProcessStatusDict.FAILED_TO_START);
////            instanceService.update(instance);
////            logger.error("error while submitting the process into workflowService", error);
////            throw new WorkflowServerException(WORKFLOW_INSTANCE_FACADE_CAN_NOT_START_PROCESS_INSTANCE,
////                    "error while submitting the process into workflowService", error);
////        }
////    }
////
////    public void submitInstance(WflProcessInstance instance) throws WorkflowServerException {
////        try {
////            Long engineId = submitProcess(instance);
////            instance.setEngineId(engineId);
////            instance.setStatus(WorkflowProcessStatusDict.SUBMITTED);
////        } catch (Exception error) {
////            instance.setStatus(WorkflowProcessStatusDict.FAILED_TO_SUBMIT);
////            instanceService.update(instance);
////            logger.error("error while submitting the process into workflowService", error);
////            throw new WorkflowServerException(CAN_NOT_SUBMIT_REQUEST,
////                    "error while submitting the process into workflowService", error);
////        }
////    }
////
////    public void startProcess(Long id) throws WorkflowServerException {
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(null));
////        KieSession kieSession = runtimeEngine.getKieSession();
////        kieSession.startProcessInstance(id);
////    }
////
////    public Long submitProcess(WflProcessInstance instance) throws WorkflowServerException {
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(null));
////        KieSession kieSession = runtimeEngine.getKieSession();
////        WorkflowRequestTypeDict key = instance.getProtoProcess().getType().getType();
////        String processId = processIdMap.get(key);
////        ProcessInstance processInstance = kieSession.createProcessInstance(processId, null);
////        return processInstance.getId();
////    }
////
////    public ProcessInstance getInstance(Long processInstanceId)
////            throws WorkflowServerException {
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(processInstanceId));
////        KieSession kieSession = runtimeEngine.getKieSession();
////        ProcessInstance processInstance = kieSession.getProcessInstance(processInstanceId, true);
////        return processInstance;
////    }
////
////    public void abortProcess(Long processInstanceId) throws WorkflowServerException {
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(processInstanceId));
////        KieSession kieSession = runtimeEngine.getKieSession();
////        kieSession.abortProcessInstance(processInstanceId);
////    }
////
////    public List<TaskSummary> getTasksForProcessByActor(
////            Long processInstanceId, List<Status> statuses, String locale,
////            WorkflowRoleDict actor) throws WorkflowServerException {
////        List<TaskSummary> resultList = new ArrayList();
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(processInstanceId));
////        TaskService ts = runtimeEngine.getTaskService();
////        List<TaskSummary> tasks = ts.getTasksByStatusByProcessInstanceId(processInstanceId, statuses, locale);
////        for (TaskSummary taskSummary : tasks) {
////            if (taskSummary.getActualOwner().getId().equals(actor.name())) {
////                resultList.add(taskSummary);
////            }
////        }
////        return resultList;
////    }
////
////    public void approveTask(
////            Long processInstanceId, String actorId, Long taskId
////    ) throws WorkflowServerException {
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(processInstanceId));
////        TaskService taskService = runtimeEngine.getTaskService();
////        taskService.start(taskId, actorId);
////        taskService.complete(taskId, actorId, Collections.<java.lang.String, Object>emptyMap());
////        context.runtime().disposeRuntimeEngine(runtimeEngine);
////    }
////
////    public void proceedProcess(WflProcessInstance instance) throws WorkflowServerException {
////        Long processInstanceId = instance.getEngineId();
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(processInstanceId));
////        TaskService ts = runtimeEngine.getTaskService();
////        List<Long> taskSummaries = ts.getTasksByProcessInstanceId(instance.getEngineId());
////        for (Long taskSummaryID : taskSummaries) {
////            Task task = ts.getTaskById(taskSummaryID);
////            if (task.getTaskData().getActualOwner().getId().equals("ORIGINATOR")) {
////                logger.debug("INITIATOR TASK STARTING...");
////                ts.start(task.getId(), "ORIGINATOR");
////                logger.debug("INITIATOR TASK COMPLETING...");
////                ts.complete(task.getId(), "ORIGINATOR", Collections.<java.lang.String, Object>emptyMap());
////                logger.debug("INITIATOR TASK FINISHED.");
////            }
////        }
////    }
////
////    public void approveTask(Long engineId, WorkflowRoleDict role) throws WorkflowServerException {
////        List<TaskSummary> tasks = getTasksForProcessByActor(engineId,
////                Arrays.asList(Status.Reserved, Status.Ready, Status.InProgress), "en-US", role);
////        for (TaskSummary taskSummary : tasks) {
////            approveTask(engineId, role.name(), taskSummary.getId());
////        }
////    }
////
////    public void terminateProcess(Long engineId) throws WorkflowServerException {
////        RuntimeEngine runtimeEngine = context.runtime().getRuntimeEngine(context.context(engineId));
////        runtimeEngine.getKieSession().abortProcessInstance(engineId);
////    }
//}
