//package com.emlogis.workflow.context;
//
//import com.emlogis.model.workflow.entities.WorkflowRequest;
//import com.emlogis.workflow.enums.WorkflowRoleDict;
//import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
//import com.emlogis.workflow.exception.ExceptionCode;
//import com.emlogis.workflow.exception.WorkflowServerException;
//import com.emlogis.workflow.listeners.PredefinedProcessListener;
//import org.apache.log4j.Logger;
//import org.kie.api.runtime.KieSession;
//import org.kie.api.runtime.manager.RuntimeEngine;
//import org.kie.api.runtime.manager.RuntimeManager;
//import org.kie.api.runtime.process.ProcessInstance;
//import org.kie.api.task.TaskService;
//import org.kie.api.task.model.Status;
//import org.kie.api.task.model.Task;
//import org.kie.api.task.model.TaskSummary;
//import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
//
//import javax.ejb.*;
//import java.util.*;
//
//import static com.emlogis.workflow.context.RuntimeEnvironmentProvider.processIdMap;
//import static com.emlogis.workflow.exception.ExceptionCode.*;
//
///**
// * Created by alexborlis on 11.02.15.
// */
//@Singleton
//@LocalBean
//@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
//@TransactionAttribute(TransactionAttributeType.REQUIRED)
//public class WorkflowEngineContext {
//
//    private final static Logger logger = Logger.getLogger(WorkflowEngineContext.class);
//
//    @EJB
//    private RuntimeEnvironmentProvider provider;
//
//    @EJB
//    private PredefinedProcessListener predefinedProcessListener;
//
//    public WorkflowEngineContext() {
//        super();
//    }
//
//    private RuntimeManager runtimeManager;
//
//    public RuntimeManager runtime() throws WorkflowServerException {
//        try {
//            if (runtimeManager == null) {
//                logger.info("Setup successful!");
//                runtimeManager =
//                        org.kie.api.runtime.manager.RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(provider.provide(), "emlogis_workflow");
//                runtimeManager.getRuntimeEngine(context(null)).getKieSession().addEventListener(predefinedProcessListener);
//
//            }
//            return runtimeManager;
//        } catch (Exception error) {
//            logger.error("Error while setup");
//            throw new WorkflowServerException(WORKFLOW_JBPM_CONTEXT_CAN_NOT_FIND_RUNTIME_MANAGER, "error while setup");
//        }
//    }
//
//    public ProcessInstanceIdContext context(Long id) {
//        return ProcessInstanceIdContext.get();
//    }
//}
