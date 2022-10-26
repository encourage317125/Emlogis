package com.emlogis.server.services;

import com.emlogis.common.facade.contract.ContractLineFacade;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.common.security.PermissionScope;
import com.emlogis.common.security.PermissionType;
import com.emlogis.common.security.Permissions;
import com.emlogis.common.services.contract.ContractService;
import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.employee.SkillService;
import com.emlogis.common.services.notification.MsgDeliveryProviderSettingsService;
import com.emlogis.common.services.notification.NotificationSettingsService;
import com.emlogis.common.services.schedule.ScheduleService;
import com.emlogis.common.services.schedule.ShiftStructureService;
import com.emlogis.common.services.shiftpattern.ShiftLengthService;
import com.emlogis.common.services.shiftpattern.ShiftTypeService;
import com.emlogis.common.services.structurelevel.SiteService;
import com.emlogis.common.services.structurelevel.TeamService;
import com.emlogis.common.services.tenant.OrganizationService;
import com.emlogis.common.services.tenant.PermissionService;
import com.emlogis.common.services.tenant.ServiceProviderService;
import com.emlogis.common.services.tenant.TenantService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.notification.*;
import com.emlogis.model.tenant.Organization;
import com.emlogis.model.tenant.Permission;
import com.emlogis.model.tenant.ServiceProvider;
import com.emlogis.model.tenant.Tenant;
import com.emlogis.rest.resources.util.SimpleQuery;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.emlogis.common.Constants.*;
import static com.emlogis.model.notification.MsgDeliveryType.EMAIL;
import static com.emlogis.model.notification.MsgProviderType.POPSMTPEmail;

@Startup
@Singleton
@DependsOn({"ShiroInitServiceBean", "EventServiceBean"})
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class StartupServiceBean implements StartupService {

    private final Logger logger = Logger.getLogger(StartupServiceBean.class);

    @EJB
    private ServiceProviderService svcProviderSvc;

    @EJB
    private PermissionService permissionSvc;

    @EJB
    private SiteService siteSvc;

    @EJB
    private TeamService teamSvc;

    @EJB
    private OrganizationService organizationServerService;

    @EJB
    private TenantService tenantService;

    @EJB
    private SkillService skillSvc;

    @EJB
    private ShiftLengthService shiftLengthService;

    @EJB
    private ShiftTypeService shiftTypeService;

    @EJB
    private EmployeeService employeeSvc;

    @EJB
    private ScheduleService scheduleSvc;

    @EJB
    private ShiftStructureService shiftStructureSvc;

    @EJB
    private ContractService contractSvc;

    @EJB
    private ScheduleFacade scheduleFacade;

    @EJB
    private ContractLineFacade contractLineFacade;

    @EJB
    private AbsenceTypeService absenceTypeSvc;

    @EJB
    private EmployeeFacade employeeFacade;

    @EJB
    OrganizationService organizationService;

    @EJB
    MsgDeliveryProviderSettingsService msgDeliveryProviderService;

    @EJB
    NotificationSettingsService settingsService;

    public StartupServiceBean() {
    }

    @PostConstruct
    void init() {
        logger.debug("***************************** StartupService - Init starting ....");

        mergeTestMsgDeliveryProviderForWorkflowNotifications();

        ServiceProvider sp = svcProviderSvc.getServiceProvider(Organization.DEFAULT_SERVICEPROVIDER_ID);
        if (sp != null) {
            logger.debug("ServiceProvider: " + Organization.DEFAULT_SERVICEPROVIDER_ID + " was created in a previous session, Skipping Db initialization");
            return;
        }

        // initialize Database:

        // create delivery providers if not existing
        getOrCreateMsgDeliveryProviders();

        // Add NotificationSettings, retry = 2, max delivery hours = 24, queue processing size = 10
        NotificationSettings settings = settingsService.createNotificationSettings(2, 24, 10, 24);

        // create permissions if not existing yetOrganizationProfileView

        createPermission(Permissions.OrganizationProfile_View, PermissionType.View, PermissionScope.Customer, "Allows a Scheduler or Employee to View the 'StructureLevel' (aka Site, Skills, and Teams) entities");
        createPermission(Permissions.OrganizationProfile_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to modify the 'StructureLevel' (aka Site, Skills, and Teams) entities");

        createPermission(Permissions.Employee_View, PermissionType.View, PermissionScope.Customer, "Allows a user to view Employees");
        createPermission(Permissions.EmployeeProfile_Update, PermissionType.Update, PermissionScope.Customer, "Allows an Employee to modify his profile");
        createPermission(Permissions.Employee_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to make changes to Employee settings");
        createPermission(Permissions.EmployeeWages_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to change other employees Wage setting");

        createPermission(Permissions.Demand_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to make changes in structurelevel for a schedule");
        createPermission(Permissions.Demand_View, PermissionType.View, PermissionScope.Customer, "Allows a Scheduler to View structurelevel for a schedule");
        createPermission(Permissions.Shift_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Shift manager to manage Shifts, ie create/edit/delete/assign/drop/post Shifts");
//        createPermission(Permissions.ShiftStructureMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to make changes to shift structures that are to fit structurelevel for a schedule");
        createPermission(Permissions.Schedule_View, PermissionType.View, PermissionScope.Customer, "Allows an Employee to view Schedules/Calendars");
        createPermission(Permissions.Schedule_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to create delete and make changes to a schedule");
        createPermission(Permissions.Schedule_Update, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to make changes to a schedule (doesn't allow Schedule create / delete)");
        createPermission(Permissions.Schedule_AdvancedMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to use/modify the Advanced Options when generating or editing a schedule");

        createPermission(Permissions.Availability_Request, PermissionType.Update, PermissionScope.Customer, "Allows the Employee to request changes to their own Availability & TimeOff");
        createPermission(Permissions.Availability_RequestMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to make changes to an employee’s Availability");
//        createPermission(Permissions.PTO_Request, PermissionType.Update, PermissionScope.Customer, "Allows the Employee to request PTO");
//        createPermission(Permissions.PTO_RequestMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to approve deny PTO requests");

//        createPermission(Permissions.OpenShift_Request, PermissionType.Update, PermissionScope.Customer, "Allows an Employee to request a Posted OpenShift");
//        createPermission(Permissions.OpenShift_RequestMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to process/handle Posted OpenShift requests");
        createPermission(Permissions.Shift_Request, PermissionType.Update, PermissionScope.Customer, "Allows an Employee to submit a SWAP/WIP/OS request");
        createPermission(Permissions.Shift_RequestMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to process/handle SWAP/WIP/OS requests");
//        createPermission(Permissions.WIP_Request, PermissionType.Update, PermissionScope.Customer, "Allows an Employee to request an other employee to Work In Place of him");
//        createPermission(Permissions.WIP_RequestMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to process/handle WIP requests");
//        createPermission(Permissions.ShiftBiddingRequest, PermissionType.Update, PermissionScope.Customer, "Allows an Employee to bid on an Open Shift");
//        createPermission(Permissions.ShiftBiddingMgmt, PermissionType.Update, PermissionScope.Customer, "Allows a Scheduler to manage ShiftBidding");

        createPermission(Permissions.Notification_Recipient, PermissionType.Update, PermissionScope.Customer, "Allows an Employee to receive Notifications");

        createPermission(Permissions.Reports_Mgmt, PermissionType.Update, PermissionScope.Customer, "Allows to create / edit delete Report Templates");
        createPermission(Permissions.Reports_View, PermissionType.View, PermissionScope.Customer, "Allows to view executed Reports");
        createPermission(Permissions.Reports_Exe, PermissionType.Update, PermissionScope.Customer, "Allows to execute and view reports");


        createPermission(Permissions.Impersonate_ViewOnly, PermissionType.View, PermissionScope.Customer, "Allows the currently logged user to 'log' as an other user without requiring credentials, and getting same permissions as impersonated user, restricted to actions that do not modify impersonated user data");
        createPermission(Permissions.Impersonate_ReadWrite, PermissionType.Update, PermissionScope.Customer, "Allows the currently logged user to 'log' as an other user without requiring credentials, and getting same permissions as impersonated user");

        createPermission(Permissions.SystemConfiguration_Mgmt, PermissionType.Update, PermissionScope.All, "Allows an admin user to perform configuration / administrative / maintenance tasks for this customer (like email settings or notification configuration, etc ..)");
        createPermission(Permissions.Account_View, PermissionType.View, PermissionScope.All, "Allows a user to view Group and User Accounts / Role Associations");
        createPermission(Permissions.Account_Mgmt, PermissionType.Update, PermissionScope.All, "allows an admin to create / edit / delete / view Group and User Accounts, add/remove Users to/from Groups and Roles to/from Accounts. Also, allows to activate/revoke accounts, reset passwords");
        createPermission(Permissions.AccountProfile_Update, PermissionType.Update, PermissionScope.Customer, "allows a user to modify its account properies (login, address, email & phones ...");
        createPermission(Permissions.Role_View, PermissionType.View, PermissionScope.All, "Allows a user to view Roles and Permissions");
        createPermission(Permissions.Role_Mgmt, PermissionType.Update, PermissionScope.All, "Allows an admin to view Permissions, create / edit / delete / view Roles");
        createPermission(Permissions.Tenant_View, PermissionType.View, PermissionScope.ServiceProvider, "Allows a Service Admin to view Tenants (customers)");
        createPermission(Permissions.Tenant_Mgmt, PermissionType.Update, PermissionScope.ServiceProvider, "Allows a Service Admin to create / edit / delete / view Tenants (customers)");

        createPermission(Permissions.Support, PermissionType.Update, PermissionScope.Customer, "Allows Support Personel to run some specific diagnostic feature");

        // create  a couple customers
        String[] orgIds = {"emlogis", "jcso", "lcso", "slh", "mcm", "tmp", "idf", "test", "htran"};
        String[] orgNames = {"EmLogis", "JCSO", "LCSO", "Saint Luke Hospital", "Montgomery County Maryland", "The Mad Potter", "IDF", "Test", "QA Data"};

        try {
            // no service provider, need to create one
            svcProviderSvc.createServiceProvider(Organization.DEFAULT_SERVICEPROVIDER_ID, "EmLogisService", "EmLogis Service");
            logger.debug("ServiceProvider: " + Organization.DEFAULT_SERVICEPROVIDER_ID + " created !!");

            for (int i = 0; i < orgIds.length; i++) {
                String tenantId = orgIds[i];
                Organization org = (Organization) svcProviderSvc.getTenant(tenantId);
                if (org == null) {
                    try {
                        org = svcProviderSvc.createOrganization(tenantId, orgNames[i], null);
//                        createHolidays(tenantId);
                        logger.debug("Organization: " + orgNames[i] + " created !!");
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.debug("********* Organization" + orgNames[i] + " Creation FAILURE *********");
                        e.printStackTrace();
                    }
                } else {
                    logger.debug("Organization: " + orgNames[i] + " was created in a previous session");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //logger.debug("***************************** StartupService - Init done !");

        logger.debug("***************************** StartupService init complete.");
    }

    private void mergeTestMsgDeliveryProviderForWorkflowNotifications() {
        MsgDeliveryProviderSettings msgDeliveryProviderSettingsEmail = null;
        Map<String, String> settingsMapEmail = null;
        MsgDeliveryProviderSettings msgDeliveryProviderSettingsSMS = null;
        Map<String, String> settingsMapSms = null;
        
        Map<String, String> tenantSettingsMapEmail = null;
        Map<String, String> tenantSettingsMapSms = null;
        
        String emaiProviderKey = System.getProperty(NOTIFICATION_PROVIDER) + EMAIL_NAME_PART;
        String smsProviderKey = System.getProperty(NOTIFICATION_PROVIDER) + SMS_NAME_PART;
        try {
            msgDeliveryProviderSettingsEmail = msgDeliveryProviderService.getMsgDeliveryProvider(emaiProviderKey);
            settingsMapEmail = msgDeliveryProviderSettingsEmail.getSettings();
        } catch (Throwable error) {
            logger.error("Cannot find msg delivery provider for notifications",error);
        }
        if (msgDeliveryProviderSettingsEmail == null) {
            // Define Email Provider
            msgDeliveryProviderSettingsEmail = new MsgDeliveryProviderSettings();
            msgDeliveryProviderSettingsEmail.setId(emaiProviderKey);
            msgDeliveryProviderSettingsEmail.setName(emaiProviderKey);
            msgDeliveryProviderSettingsEmail.setDescription("Email settings provider for sending email notifications");
            msgDeliveryProviderSettingsEmail.setDeliveryType(EMAIL);
            msgDeliveryProviderSettingsEmail.setProviderType(POPSMTPEmail);
            msgDeliveryProviderSettingsEmail.setActive(true);
            settingsMapEmail = new HashMap<>();
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDHOST, "mail.emlogis.net");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDPORT, "25");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEHOST, "mail.emlogis.net");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEPORT, "7993");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_USERNAME, "devmail@emlogis.net");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_PASSWORD, "EmLogis123");
            settingsMapEmail.put(MsgDeliveryProviderSettings.TENANT_MAILBOX, "mercury.emlogis.com");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_FROM, System.getProperty(NOTIFICATION_DEV_FROM));
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_REPLYTO, System.getProperty(NOTIFICATION_DEV_REPLY_TO));
            msgDeliveryProviderSettingsEmail.setSettings(settingsMapEmail);
            msgDeliveryProviderSettingsEmail.setStatus(MsgDeliveryProviderStatus.OK);
            msgDeliveryProviderSettingsEmail.setStatusInfo("Status OK");
            msgDeliveryProviderService.getEntityManager().persist(msgDeliveryProviderSettingsEmail);
        }
        try {
            msgDeliveryProviderSettingsSMS = msgDeliveryProviderService.getMsgDeliveryProvider(smsProviderKey);
            settingsMapSms = msgDeliveryProviderSettingsSMS.getSettings();
        } catch (Throwable error) {
            logger.error("Cannot find msg delivery provider for notifications",error);
        }
        // Define SMS Provider
        if (msgDeliveryProviderSettingsSMS == null) {
            msgDeliveryProviderSettingsSMS = new MsgDeliveryProviderSettings();
            msgDeliveryProviderSettingsSMS.setId(smsProviderKey);
            msgDeliveryProviderSettingsSMS.setName(smsProviderKey);
            msgDeliveryProviderSettingsSMS.setDescription("SMS settings provider for sending email notifications");
            msgDeliveryProviderSettingsSMS.setDeliveryType(MsgDeliveryType.SMS);
            msgDeliveryProviderSettingsSMS.setProviderType(MsgProviderType.Twillio);
            msgDeliveryProviderSettingsSMS.setActive(true);
            settingsMapSms = new HashMap<>();
            settingsMapSms.put(msgDeliveryProviderSettingsSMS.TWILIO_PROVIDER_ACCOUNTID, "AC0d8f962374d04d35ad50ca7b5c97caa2");
            settingsMapSms.put(msgDeliveryProviderSettingsSMS.TWILIO_PROVIDER_SUBACCOUNTID, "AC1ab1c5257e072918725841a3dacaca32");
            settingsMapSms.put(msgDeliveryProviderSettingsSMS.TWILIO_PROVIDER_AUTHKEY, "cc6f45dfde026083bf04f9376cfa8b88");
            settingsMapSms.put(msgDeliveryProviderSettingsSMS.TWILIO_PROVIDER_FROMNUMBER, "(650) 204-4649");
            msgDeliveryProviderSettingsSMS.setSettings(settingsMapSms);
            msgDeliveryProviderSettingsSMS.setStatus(MsgDeliveryProviderStatus.OK);
            msgDeliveryProviderSettingsSMS.setStatusInfo("Status OK");
            msgDeliveryProviderService.getEntityManager().persist(msgDeliveryProviderSettingsSMS);
        }

        Collection<Organization> organizationResultSet =
                organizationServerService.findOrganizations(new SimpleQuery()).getResult();
        for (Organization organization : organizationResultSet) {
            //for email
            Tenant tenant = tenantService.getTenant(organization.getTenantId());
            String tenantId = tenant.getTenantId();
            
            MsgDeliveryTenantSettings messageDeliveryTS = tenant.getEmailDeliveryTenantSettings();
            
            if(messageDeliveryTS == null) {
            	messageDeliveryTS = new MsgDeliveryTenantSettings(
                        new PrimaryKey(organization.getTenantId()), MsgDeliveryType.EMAIL
                );
            }
                        
            settingsMapEmail.put(MsgDeliveryProviderSettings.TENANT_MAILBOX, tenantId + ".mercury.emlogis.com");
            settingsMapEmail.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_FROM, tenantId + "@emlogis.com"); 
            
            tenantSettingsMapEmail = new HashMap<>(settingsMapEmail);
                                   
            messageDeliveryTS.setSettings(tenantSettingsMapEmail);
            messageDeliveryTS.setStatus(MsgDeliveryProviderStatus.OK);
            messageDeliveryTS.setDeliveryProviderSettings(msgDeliveryProviderSettingsEmail);
            msgDeliveryProviderService.getEntityManager().persist(messageDeliveryTS);
            tenant.setEmailDeliveryTenantSettings(messageDeliveryTS);
            
            // for sms
            MsgDeliveryTenantSettings messageDeliveryTSms = tenant.getSmsDeliveryTenantSettings();
            
            if(messageDeliveryTSms == null) {
            	messageDeliveryTSms = new MsgDeliveryTenantSettings(
                        new PrimaryKey(organization.getTenantId()), MsgDeliveryType.SMS);
            }
            
            tenantSettingsMapSms = new HashMap<>(settingsMapSms);
            
            tenantSettingsMapSms.put(MsgDeliveryTenantSettings.TWILIO_PROVIDER_FROMNUMBER,
            		settingsMapSms.get(MsgDeliveryProviderSettings.TWILIO_PROVIDER_FROMNUMBER));
 
            messageDeliveryTSms.setSettings(tenantSettingsMapSms);
            
            tenantSettingsMapSms.remove(MsgDeliveryProviderSettings.TWILIO_PROVIDER_FROMNUMBER);
                        
            messageDeliveryTSms.setStatus(MsgDeliveryProviderStatus.OK);
            messageDeliveryTSms.setDeliveryProviderSettings(msgDeliveryProviderSettingsSMS);
            msgDeliveryProviderService.getEntityManager().persist(messageDeliveryTSms);
            tenant.setSmsDeliveryTenantSettings(messageDeliveryTSms);

            msgDeliveryProviderService.getEntityManager().merge(tenant);
        }
    }

    /*
        private Employee createEmployee(String tenantId, String firstName, String lastName, String employeeId)
                throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            EmployeeCreateDto createDto = new EmployeeCreateDto();
            createDto.setFirstName(firstName);
            createDto.setLastName(lastName);
            createDto.setEmployeeIdentifier(employeeId);

            PrimaryKey pk = new PrimaryKey(tenantId, firstName+lastName);
            Employee employee = employeeSvc.createEmployee(pk, firstName, lastName, employeeId);

            PrimaryKey contractKey = new PrimaryKey(employee.getPrimaryKey().getTenantId());
            EmployeeContract employeeContract = contractSvc.createEmployeeContract(contractKey);
            employeeContract.setEmployee(employee);
            employeeContract.setDefaultContract(true);
            employeeContract = (EmployeeContract) contractSvc.updateContract(employeeContract);
            employee.getEmployeeContracts().add(employeeContract);
            employeeSvc.update(employee);
            return employee;
        }

        private void addDefaultContractLines(Employee employee) {

            Set<EmployeeContract> contracts = employee.getEmployeeContracts();
            Iterator<EmployeeContract> it = contracts.iterator();
            if (!it.hasNext()) {
                return;		// uanble to find contract for employee ...
            }
            EmployeeContract contract = it.next();
            String contractId = contract.getId();

            ContractLineCreateDto createDto;
            ContractLineDTO dto;
            try {
                createDto = new ContractLineCreateDto();
                createDto.setContractId(contractId);
                createDto.setContractLineType(ContractLineType.DAYS_PER_WEEK);
                createDto.setMinimumEnabled(true);
                createDto.setMinimumValue(3);
                createDto.setMinimumWeight(1);
                createDto.setMaximumEnabled(true);
                createDto.setMaximumValue(5);
                createDto.setMaximumWeight(1);
                dto = contractLineFacade.createObject(new PrimaryKey(employee.getTenantId()),createDto);

                createDto.setContractLineType(ContractLineType.HOURS_PER_DAY);
                createDto.setMinimumEnabled(true);
                createDto.setMinimumValue(240);
                createDto.setMinimumWeight(1);
                createDto.setMaximumEnabled(true);
                createDto.setMaximumValue(480);
                createDto.setMaximumWeight(1);
                dto = contractLineFacade.createObject(new PrimaryKey(employee.getTenantId()),createDto);

                createDto.setContractLineType(ContractLineType.HOURS_BETWEEN_DAYS);
                createDto.setMinimumEnabled(true);
                createDto.setMinimumValue(60);
                createDto.setMinimumWeight(1);
                createDto.setMaximumEnabled(false);
                createDto.setMaximumValue(240);
                createDto.setMaximumWeight(1);
                dto = contractLineFacade.createObject(new PrimaryKey(employee.getTenantId()),createDto);

                // add a pattern contract line
                createDto = new ContractLineCreateDto();
                createDto.setContractId(contractId);
                createDto.setContractLineType(ContractLineType.CUSTOM);
                createDto.setDayOfWeek(DayOfWeek.FRIDAY);
                createDto.setNumberOfDays(2);
                createDto.setOutOfTotalDays(7);
                createDto.setRotationType(RotationPatternType.DAYS_OFF_PATTERN);
                createDto.setWeight(1);
                dto = contractLineFacade.createObject(new PrimaryKey(employee.getTenantId()),createDto);

            } catch (InstantiationException | IllegalAccessException
                    | NoSuchMethodException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }

        }

        private ShiftReqOld addShiftReq(ShiftStructure st, int dayIndex, int hour, int min, int length,
                                        ShiftLength shiftLength, String skillId, int skillLevel, int employeeNb) {

            ShiftReqOld shiftReq = new ShiftReqOld();
            shiftReq.setPrimaryKey(new PrimaryKey(st.getTenantId()));
            shiftReq.setShiftStructureId(st.getPrimaryKey().getId());

            shiftReq.setShiftLength(shiftLength);
            shiftReq.setDayIndex(dayIndex);
            shiftReq.setStartTime(new LocalTime(hour, min));
            shiftReq.setDurationInMins(length);
            shiftReq.setExcess(false);
            shiftReq.setSkillId(skillId);
            shiftReq.setSkillProficiencyLevel(skillLevel);
            shiftReq.setEmployeeCount(employeeNb);

            shiftStructureSvc.persistShiftReq(shiftReq);
            st.getShiftReqs().add(shiftReq);
            shiftStructureSvc.update(st);
            return shiftReq;
        }
    */
    private void createPermission(Permissions permission, PermissionType type, PermissionScope scope, String description) {
        Permission perm = permissionSvc.getPermissionByKey(permission);
        if (perm == null) {
            permissionSvc.createPermission(permission, type, scope, description);
        }
    }

    private void getOrCreateMsgDeliveryProviders() {
        MsgDeliveryProviderSettings twilio = msgDeliveryProviderService.getMsgDeliveryProvider(MsgDeliveryProviderSettings.TWILIO_PROVIDER_ID);
        if (twilio == null) {
            twilio = msgDeliveryProviderService.createMsgDeliveryProviderSettings(MsgDeliveryProviderSettings.TWILIO_PROVIDER_ID);
            twilio.setName("Twilio");
            twilio.setDescription("Twilio SMS connector");
            twilio.setDeliveryType(MsgDeliveryType.SMS);
            twilio.setProviderType(MsgProviderType.Twillio);
            Map<String, String> settings = new HashMap();
            settings.put(MsgDeliveryProviderSettings.TWILIO_PROVIDER_ACCOUNTID, "access id");
            settings.put(MsgDeliveryProviderSettings.TWILIO_PROVIDER_AUTHKEY, "access key");
//			settings.put(MsgDeliveryProviderSettings.TWILIO_PROVIDER_FROMNUMBER, "(nnn) nnn nnnn");
            twilio.setSettings(settings);
        }
        MsgDeliveryProviderSettings primEmail = msgDeliveryProviderService.getMsgDeliveryProvider(MsgDeliveryProviderSettings.PRIMARYEMAIL_PROVIDER_ID);
        if (primEmail == null) {
            primEmail = msgDeliveryProviderService.createMsgDeliveryProviderSettings(MsgDeliveryProviderSettings.PRIMARYEMAIL_PROVIDER_ID);
            primEmail.setName("Primary Email");
            primEmail.setDescription("Primary Email Server");
            primEmail.setDeliveryType(EMAIL);
            primEmail.setProviderType(POPSMTPEmail);
            Map<String, String> settings = new HashMap();
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDHOST, "send host name");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDPORT, "25");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEHOST, "receive host name");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEPORT, "7993");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_USERNAME, "user name");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_PASSWORD, "password");
            primEmail.setSettings(settings);
        }
        MsgDeliveryProviderSettings secondaryEmail = msgDeliveryProviderService.getMsgDeliveryProvider(MsgDeliveryProviderSettings.SECONDARYEMAIL_PROVIDER_ID);
        if (secondaryEmail == null) {
            secondaryEmail = msgDeliveryProviderService.createMsgDeliveryProviderSettings(MsgDeliveryProviderSettings.SECONDARYEMAIL_PROVIDER_ID);
            secondaryEmail.setName("Secondary Email");
            secondaryEmail.setDescription("Secondary Email Server");
            secondaryEmail.setDeliveryType(EMAIL);
            secondaryEmail.setProviderType(POPSMTPEmail);
            Map<String, String> settings = new HashMap();
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDHOST, "aux send host name");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_SENDPORT, "25");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEHOST, "aux receive host name");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_RECEIVEPORT, "7993");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_USERNAME, "user name");
            settings.put(MsgDeliveryProviderSettings.POPSMTP_PROVIDER_PASSWORD, "password");
            secondaryEmail.setSettings(settings);
        }
    }
/*    
    private void createHolidays(String tenantId) {
    	
        Organization org = organizationService.getOrganization(tenantId);
    	
        PrimaryKey primaryKey = new PrimaryKey(org.getTenantId());
        Holiday holiday = new Holiday(primaryKey);
        holiday.setName("March 11");
		DateTime dt = new DateTime(2015, 3, 11, 0, 0, 0, 0);
        holiday.setEffectiveStartDate(dt.getMillis());
        holiday.setAbbreviation("3/11");
        holiday.setEffectiveEndDate(dt.getMillis());O
        holiday.setTimeToDeductInMin(0);
        organizationService.addHoliday(org, holiday);

        primaryKey = new PrimaryKey(org.getTenantId());
        holiday = new Holiday(primaryKey);
        holiday.setName("Valentine's day");
		dt = new DateTime(2015, 2, 14, 0, 0, 0, 0);
        holiday.setEffectiveStartDate(dt.getMillis());
        holiday.setAbbreviation("2/14");
        holiday.setEffectiveEndDate(dt.getMillis());
        holiday.setTimeToDeductInMin(0);
        organizationService.addHoliday(org, holiday);
        

        primaryKey = new PrimaryKey(org.getTenantId());
        holiday = new Holiday(primaryKey);
        holiday.setName("Test period 27-29");
		dt = new DateTime(2015, 1, 27, 0, 0, 0, 0);
        holiday.setEffectiveStartDate(dt.getMillis());
        holiday.setAbbreviation("1/29");
		dt = new DateTime(2015, 1, 29, 0, 0, 0, 0);
        holiday.setEffectiveEndDate(dt.getMillis());
        holiday.setTimeToDeductInMin(0);
        organizationService.addHoliday(org, holiday);
        
    }
*/
}
