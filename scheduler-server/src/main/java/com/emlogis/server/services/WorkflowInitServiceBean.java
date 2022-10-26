package com.emlogis.server.services;

import com.emlogis.common.FileUtil;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.workflow.process.proto.WflProcessService;
import com.emlogis.common.services.workflow.roles.WflRoleService;
import com.emlogis.common.services.workflow.templates.WflScriptService;
import com.emlogis.common.services.workflow.type.WflProcessTypeService;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WflProcessType;
import com.emlogis.model.workflow.entities.WflRole;
import com.emlogis.model.workflow.entities.WflSourceScript;
import com.emlogis.rest.resources.util.SimpleQuery;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.WorkflowRoleDict;
import com.emlogis.workflow.exception.WorkflowServerException;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.emlogis.workflow.enums.WorkflowRequestTypeDict.*;

/**
 * Created by alexborlis on 29.01.15.
 */
@Startup
@Singleton
@DependsOn({"StartupServiceBean"})
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WorkflowInitServiceBean implements IWorkflowInitServiceBean {

    private final static Logger logger = Logger.getLogger(WorkflowInitServiceBean.class);
    private static final ConcurrentHashMap<String, String> initialResourceMap;
    static {
        initialResourceMap = new ConcurrentHashMap<>();
        initialResourceMap.put(TIME_OFF_REQUEST.name(), "pto_process.bpmn2");
        initialResourceMap.put(OPEN_SHIFT_REQUEST.name(), "short_open_shift_process.bpmn2");
        initialResourceMap.put(SHIFT_SWAP_REQUEST.name(), "shift_swap_process.bpmn2");
        initialResourceMap.put(WIP_REQUEST.name(), "work_in_place_process.bpmn2");
        initialResourceMap.put(AVAILABILITY_REQUEST.name(), "availability_process.bpmn2");
    }


    @EJB
    private WflScriptService templateRepository;

    @EJB
    private WflProcessTypeService processTypeService;

    @EJB
    private WflProcessService processService;

    @EJB
    private WflRoleService protoRoleService;

    @EJB
    private OrganizationService organizationServerService;

    @PostConstruct
    void init() {
        logger.info("Start setup of predefined processes");
        try {
            setupDictionaries();
           // setupOrganizations();
            Collection<Organization> organizationResultSet =
                    organizationServerService.findOrganizations(new SimpleQuery()).getResult();
            Map<WflProcessType, WflSourceScript> templatesMap = uploadTemplates();
            logger.info("Found " + organizationResultSet.size() + " organizations to setup predefined processes.");
            for (Organization organizationEntity : organizationResultSet) {
                logger.info(organizationEntity.toString());
                Iterator<Map.Entry<String, String>> iterator = initialResourceMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    WflProcessType processType = findWorkflowTypeByDictionary(WorkflowRequestTypeDict.valueOf(entry.getKey()));
                    String processName = generateName(organizationEntity.getName(), WorkflowRequestTypeDict.valueOf(entry.getKey()));
                    WflProcess process = processService.merge(new WflProcess(processName, organizationEntity.getTenantId(),
                            templatesMap.get(processType), processType));
                    process.getRoles().addAll(findRoles(WorkflowRequestTypeDict.valueOf(entry.getKey()), process));
                    process = processService.update(process);
                    logger.info("process created: " + process.toString());
                }
            }
        } catch (Exception exception) {
            logger.error("Fatal error while setup predefined processes",exception);
            System.exit(0);
        }
    }

    private Map<WflProcessType, WflSourceScript> uploadTemplates() throws IOException, URISyntaxException, WorkflowServerException {
        Map<WflProcessType, WflSourceScript> resultMap = new HashMap<>();
        Iterator<Map.Entry<String, String>> iterator = initialResourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            WflSourceScript templateEntity = template(entry);
            templateEntity = merge(templateEntity);
            resultMap.put(processTypeService.findByDictionary(WorkflowRequestTypeDict.valueOf(entry.getKey())), templateEntity);
        }
        return resultMap;
    }

    private WflSourceScript merge(WflSourceScript entity) {
        List<WflSourceScript> templates = templateRepository.findAllByName(entity.getName());
        if (templates.isEmpty()) {
            return templateRepository.create(entity);
        } else {
            return templates.get(0);
        }
    }

    private WflSourceScript template(Map.Entry<String, java.lang.String> entry
    ) throws IOException, URISyntaxException, WorkflowServerException {
        java.lang.String finalTemplate = FileUtil.readFileContentByName(entry.getValue());
        WflProcessType wflProcessType = findWorkflowTypeByDictionary(WorkflowRequestTypeDict.valueOf(entry.getKey()));
        WflSourceScript sourceScript = new WflSourceScript(entry.getKey(), wflProcessType, finalTemplate.getBytes("UTF-8"));
        return sourceScript;
    }

    private void setupDictionaries() {
        try {
            WflProcessType pto = new WflProcessType("Paid Time Off Request", TIME_OFF_REQUEST);
            processTypeService.merge(pto);

            WflProcessType os = new WflProcessType("Open Shift Request", OPEN_SHIFT_REQUEST);
            processTypeService.merge(os);


            WflProcessType ss = new WflProcessType("Shift Swap Request", SHIFT_SWAP_REQUEST);
            processTypeService.merge(ss);

            WflProcessType wip = new WflProcessType("Work in Place Request", WIP_REQUEST);
            processTypeService.merge(wip);

            WflProcessType ar = new WflProcessType("Availability Request", AVAILABILITY_REQUEST);
            processTypeService.merge(ar);


            WflRole originatorRole = new WflRole("ORIGINATOR", WorkflowRoleDict.ORIGINATOR);
            protoRoleService.merge(originatorRole);

            WflRole recipient = new WflRole("PEER", WorkflowRoleDict.PEER);
            protoRoleService.merge(recipient);

            WflRole manager = new WflRole("MANAGER", WorkflowRoleDict.MANAGER);
            protoRoleService.merge(manager);

        } catch (Exception error) {
            logger.error("ERROR  while setup workflow dictionaries", error);
            System.exit(0);
        }
    }

    private List<WflRole> findRoles(WorkflowRequestTypeDict key, WflProcess process) {
        switch (key) {
            case TIME_OFF_REQUEST:
                return getRolesByRoleTypes(new ArrayList<>(Arrays.asList(WorkflowRoleDict.ORIGINATOR, WorkflowRoleDict.MANAGER)), process);
            case OPEN_SHIFT_REQUEST:
                return getRolesByRoleTypes(new ArrayList<>(Arrays.asList(WorkflowRoleDict.ORIGINATOR, WorkflowRoleDict.PEER, WorkflowRoleDict.MANAGER)), process);
            case SHIFT_SWAP_REQUEST:
                return getRolesByRoleTypes(new ArrayList<>(Arrays.asList(WorkflowRoleDict.ORIGINATOR, WorkflowRoleDict.PEER, WorkflowRoleDict.MANAGER)), process);
            case WIP_REQUEST:
                return getRolesByRoleTypes(new ArrayList<>(Arrays.asList(WorkflowRoleDict.ORIGINATOR, WorkflowRoleDict.PEER, WorkflowRoleDict.MANAGER)), process);
            case AVAILABILITY_REQUEST:
                return getRolesByRoleTypes(new ArrayList<>(Arrays.asList(WorkflowRoleDict.ORIGINATOR, WorkflowRoleDict.MANAGER)), process);
        }
        return null;
    }

    private List<WflRole> getRolesByRoleTypes(List<WorkflowRoleDict> roles, WflProcess process) {
        List<WflRole> rolesResult = new ArrayList<>();
        for (WorkflowRoleDict roleDict : roles) {
            //WflRole wflRole = protoRoleService.merge(roleDict, process);
            //WflRole wflRole = new WflRole(roleDict.name(), roleDict, process);
            WflRole role = protoRoleService.findByName(roleDict.name());
            rolesResult.add(role);
        }
        return rolesResult;
    }

    private java.lang.String generateName(java.lang.String organizationName, WorkflowRequestTypeDict dictionary) {
        return new java.lang.String(organizationName + "_" + dictionary.name());
    }

    private WflProcessType findWorkflowTypeByDictionary(WorkflowRequestTypeDict dictionary) {
        return processTypeService.findByDictionary(dictionary);
    }
}
