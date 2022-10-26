//package com.emlogis.workflow.context;
//
//import com.emlogis.common.Constants;
//import com.emlogis.common.services.workflow.templates.WflScriptService;
//import com.emlogis.common.services.workflow.type.WflProcessTypeService;
//import com.emlogis.model.workflow.entities.WflProcessType;
//import com.emlogis.model.workflow.entities.WflSourceScript;
//import com.emlogis.workflow.callback.EmlogisUserGroupCallback;
//import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
//import com.emlogis.workflow.exception.WorkflowServerException;
//import com.emlogis.workflow.persistence.EmlogisWFLTransactionManager;
//import org.apache.log4j.Logger;
//import org.drools.persistence.PersistenceContextManager;
//import org.jbpm.persistence.JpaProcessPersistenceContextManager;
//import org.jbpm.runtime.manager.impl.RuntimeEnvironmentBuilder;
//import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
//import org.jbpm.services.task.persistence.JPATaskPersistenceContextManager;
//import org.kie.api.io.Resource;
//import org.kie.api.runtime.EnvironmentName;
//import org.kie.internal.io.ResourceFactory;
//import org.kie.internal.task.api.TaskPersistenceContextManager;
//
//import javax.annotation.PostConstruct;
//import javax.ejb.*;
//import javax.naming.InitialContext;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.*;
//import static com.emlogis.workflow.exception.ExceptionCode.WORKFLOW_CAN_NOT_SETUP_JBPM_ENVIRONMENT;
//import static org.kie.api.io.ResourceType.BPMN2;
//
///**
// * Created by alexborlis on 16.02.15.
// */
//@Singleton
//@LocalBean
//@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
//@TransactionAttribute(TransactionAttributeType.REQUIRED)
//public class RuntimeEnvironmentProvider {
//
//    private final static Logger logger = Logger.getLogger(RuntimeEnvironmentProvider.class);
//
//    public enum RuntimeConfiguration {
//        SINGLETON,
//        PER_REQUEST,
//        PER_PROCESS
//    }
//
//    static final ConcurrentHashMap<WorkflowRequestTypeDict, String> processIdMap;
//
//    static {
//        processIdMap = new ConcurrentHashMap<>();
//        processIdMap.put(TIME_OFF_REQUEST, "pto_process");
//        processIdMap.put(OPEN_SHIFT_REQUEST, "short_open_shift_process");
//        processIdMap.put(SHIFT_SWAP_REQUEST, "shift_swap_process");
//        processIdMap.put(WIP_REQUEST, "work_in_place_process");
//        processIdMap.put(AVAILABILITY_REQUEST, "availability_process");
//    }
//
//    private static ConcurrentHashMap<WflProcessType, WflSourceScript> templateMap = new ConcurrentHashMap<>();
//
//    @EJB
//    private EmlogisUserGroupCallback emlogisUserGroupCallback;
//
//    @EJB
//    private WflScriptService workflowTemplateService;
//
//    @EJB
//    private WflProcessTypeService processTypeService;
//
//    @EJB
//    private EmlogisWFLTransactionManager emlogisWFLTransactionManager;
//
//    @PersistenceContext(unitName = Constants.EMLOGIS_PERSISTENCE_UNIT_NAME)
//    private EntityManager em;
//
//    public RuntimeEnvironmentProvider() {
//    }
//
//    @PostConstruct
//    public void init() {
//        setupTemplates();
//    }
//
//    public org.kie.api.runtime.manager.RuntimeEnvironment provide() throws WorkflowServerException {
//        try {
//            org.kie.api.runtime.manager.RuntimeEnvironmentBuilder builder =
//                    RuntimeEnvironmentBuilder.getDefault()
//                            .userGroupCallback(emlogisUserGroupCallback)
//                            .entityManagerFactory(em.getEntityManagerFactory());
//            Set<Map.Entry<WflProcessType, WflSourceScript>> set = templateMap.entrySet();
//            for (Map.Entry<WflProcessType, WflSourceScript> entry : set) {
//                builder.addAsset(parseToResource(entry.getValue()), BPMN2);
//            }
//            //org.kie.api.runtime.manager.RuntimeManagerFactory factory = RuntimeManagerFactory.Factory.get();
//            org.kie.api.runtime.manager.RuntimeEnvironment env = builder.get();
//            logger.info("Start setup ENVIRONMENT context for JBPM");
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(EnvironmentName.USE_PESSIMISTIC_LOCKING, false);
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(EnvironmentName.TRANSACTION_MANAGER,
//                    emlogisWFLTransactionManager);
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(EnvironmentName.TASK_USER_GROUP_CALLBACK,
//                    emlogisUserGroupCallback);
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(EnvironmentName.ENTITY_MANAGER_FACTORY,
//                    em.getEntityManagerFactory());
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(EnvironmentName.USE_PESSIMISTIC_LOCKING, false);
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY,
//                    new InitialContext().lookup("java:jboss/TransactionSynchronizationRegistry"));
//
//            PersistenceContextManager persistenceContextMng = new JpaProcessPersistenceContextManager(env.getEnvironment());
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(
//                    EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, persistenceContextMng);
//            TaskPersistenceContextManager taskPersistenceContextMng = new JPATaskPersistenceContextManager(env.getEnvironment());
//            ((SimpleRuntimeEnvironment) env).getEnvironmentTemplate().set(
//                    EnvironmentName.TASK_PERSISTENCE_CONTEXT_MANAGER, taskPersistenceContextMng);
//            return env;
//        } catch (Exception error) {
//            logger.error("Error while setup");
//            throw new WorkflowServerException(WORKFLOW_CAN_NOT_SETUP_JBPM_ENVIRONMENT, "error while setup");
//        }
//    }
//
//    private void setupTemplates() {
//        try {
//            List<WflSourceScript> templates = workflowTemplateService.findAll();
//            for (WflSourceScript template : templates) {
//                templateMap.put(template.getType(), template);
//            }
//        } catch (Exception error) {
//            logger.error("ERROR IN SETUP WORKFLOW TEMPLATES");
//        }
//    }
//
//    private Resource parseToResource(WflSourceScript template) {
//        return ResourceFactory.newByteArrayResource(template.getTemplate());
//    }
//}
