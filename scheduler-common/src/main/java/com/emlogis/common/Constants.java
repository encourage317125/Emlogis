package com.emlogis.common;

import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Constants {
	
	public static final String EMLOGIS_PERSISTENCE_UNIT_NAME = "EGS";
	public static final String TRACKING_PERSISTENCE_UNIT_NAME = "EGST";
	public static final String EMLOGIS_IMPORT_UNIT_NAME = "EGSI";

    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String VALUE = "value";
    public static final String DATE_BEFORE = "before";
    public static final String DATE_AFTER = "after";
    public static final String DATE_PATTERN = "datePattern";
    public static final String PASS_NULL = "passNull";
    public static final String NOT_NULL_NUMBER = "notNullNumber";
    public static final String FIELD_NAMES = "fieldNames";

    public static final String ID = "id";
    public static final String TENANT_ID = "tenantId";
    public static final String PRIMARY_KEY = "primaryKey";
    public static final String EMAIL = "email";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";

    public static final String FIELD_NAME = "fieldName";
    public static final String CASE_SENSITIVE = "caseSensitive";
    public static final String REGEX = "regex";
    public static final String ORDER_DIR = "orderDir";
    public static final String ORDER_BY = "orderBy";

    public static final String NAME = "name";

    public static final String EMAIL_REGEX = "^([a-z0-9'_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
    public static final String USERNAME_REGEX = "^[ a-z0-9'+,_\\.-]{3,60}[@]{0,1}[\\da-z+_\\.-]*[\\.]*[a-z]{0,6}$";
    public static final String PASSWORD_REGEX = "^[a-z0-9_-]{6,18}$";

	public static final String DEFAULT_CONTRACT_NAME = "default";

	public static final long DATE_2000_01_01 = 946677600000L;

    public static final String TENANT_SERVICE = "tenantService";
    public static final String RESOURCES_BUNDLE = "resourcesBundle";
    
    // This is needed due to DST
    public static final int MAX_MINUTES_PER_DAY = 1500;
    public static final int MIN_MINUTES_PER_DAY = 1380;
    
    // Notification Constants
    
    public static final int FIVE_MINUTES = 1000 * 60 * 5;
    public static final int TEN_MINUTES = 1000 * 60 * 10;
    public static final long _30_DAYS_MILLISECONDS = 30L * 24L *60L * 60L * 1000;
    public static final long WEEK_MILLISECONDS = 7L * 24 * 60 * 60 * 1000;
    public static final long DAY_MILLISECONDS = 24L * 60 * 60 * 1000;
    public static final long TWO_WEEK_MILLISECONDS = 2 * WEEK_MILLISECONDS;

    public static final int TEN_SECONDS = 1000 * 10;
    public static final int FIVE_SECONDS = 1000 * 5;
    
    public static final int MIN_SMS_CODE = 11111;
    public static final int MAX_SMS_CODE = 99999;
    
    public static final int NOTIFICATION_EMAIL_MATCH_LENGTH = 50;
    
    public static final String REG_EXP_YES = "((YES|Y)[ ]{0,}[0-9]{5})|([0-9]{5}[ ]{0,}(YES|Y))";
    public static final String REG_EXP_NO = "((NO|N)[ ]{0,}[0-9]{5})|([0-9]{5}[ ]{0,}(NO|N))";
    public static final String REG_EXP_FOR_DIGITS="\\d{5}";
    
    public static final Pattern REG_EXP_YES_PATTERN = Pattern.compile(REG_EXP_YES,Pattern.CASE_INSENSITIVE);
    public static final Pattern REG_EXP_NO_PATTERN = Pattern.compile(REG_EXP_NO,Pattern.CASE_INSENSITIVE);
    public static final Pattern REG_EXP_FOR_DIGITS_PATTERN = Pattern.compile(REG_EXP_FOR_DIGITS,Pattern.CASE_INSENSITIVE);
    
    public static final String LOG_NOTIFICATIONS = "log.notifications";

    public static final String NOTIFICATION_MODE = "notification.mode";
    public static final String NOTIFICATION_PROVIDER = "workflow.notification.provider";
    
    public static final String NOTIFICATION_MODE_DEV = "DEVELOPMENT_MODE";
    public static final String NOTIFICATION_MODE_PROD = "PRODUCTION_MODE";
    
    public static final String NOTIFICATION_RESPONSE_CODE = "UserResponseCode";
    
    public static final String EMAIL_NAME_PART = "_email";
    public static final String SMS_NAME_PART = "_sms";

    public static final String NOTIFICATION_DEV_TO = "notification.dev.to";
    public static final String NOTIFICATION_DEV_FROM = "notification.dev.from";
    public static final String NOTIFICATION_DEV_REPLY_TO = "notification.dev.reply.to";
    public static final String NOTIFICATION_DEV_SMS_NUMBER = "log.number";
    public static final String NOTIFICATION_DEV_CONSUMER = "notification.service.consumer";
    public static final String TWILIO_PROVIDER_SUBACCOUNTID = "AC1ab1c5257e072918725841a3dacaca32";

    public static final String NOTIFICATION_QA_TO = "notification.qa.to";
    public static final String NOTIFICATION_QA_FROM = "notification.qa.from";
    public static final String NOTIFICATION_QA_REPLY_TO = "notification.qa.reply.to";

    public static final String NOTIFICATION_EMAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    public static final String NOTIFICATION_EMAIL_SMTP_AUTH_PROPERTY = "mail.smtp.auth";
    public static final String NOTIFICATION_EMAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    public static final String NOTIFICATION_EMAIL_SMTP_STARTTLS_PROPERTY = "mail.smtp.starttls.enable";

    public static final String NOTIFICATION_EMAIL_MSGDELIVERY_SMTP_HOST_PROP_NAME = "sendHost";
    public static final String NOTIFICATION_EMAIL_MSGDELIVERY_SMTP_AUTH_ENABLED_PROP_NAME = "smtpAuthEnabled";
    public static final String NOTIFICATION_EMAIL_MSGDELIVERY_SMTP_PORT_PROP_NAME = "sendPort";
    public static final String NOTIFICATION_EMAIL_MSGDELIVERY_SMTP_STARTTLS_USE_PROP_NAME = "smtpStartTlsEnabled";
    public static final String NOTIFICATION_EMAIL_MSGDELIVERY_SMTP_USERNAME_PROP_NAME = "userName";
    public static final String NOTIFICATION_EMAIL_MSGDELIVERY_SMTP_PASSWORD_PROP_NAME = "password";
    
    public static final String NOTIFICATION_EMPTY_VALUE = "empty value";
    
    public static final String FILE_PATH = "filePath";
    public static final String lANGUAGE = "language";
    public static final String NOTIFICATION_OPERATION = "notificationOperation";
    public static final String NOTIFICATION_CATEGORY = "notificationCategory";
    public static final String NOTIFICATION_ROLE = "notificationRole";
    
    public static final String HTML_MATCH = "_html_";
    public static final String TEXT_MATCH = "_text_";
    public static final String SMS_MATCH = "_sms_";
    public static final String BODY_MATCH = "_body_";
    public static final String SUBJECT_MATCH = "_subject_";
    
    public static final String DELIVERY_FORMAT = "deliveryFormat";
    public static final String MESSAGE_PART = "messagePart";
    
    public static final String NMESSAGE_NO_USER = "notification.message.user.null";
    public static final String NMESSAGE_NO_SITE = "notification.message.site.null";
    public static final String NMESSAGE_NO_TENANT = "notification.message.tenant.null";
    public static final String NMESSAGE_USER_NOTIF_DISABLED = "notification.message.user.nofication.disabled";
    public static final String NMESSAGE_SITE_NOTIF_DISABLED = "notification.message.site.nofication.disabled";
    public static final String NMESSAGE_USER_ACC_DISABLED = "notification.message.user.account.disabled";
    public static final String NMESSAGE_USER_CONFIG_DISABLED = "notification.message.user.config.disabled";
    public static final String NMESSAGE_USER_TENANT_NOT_CONFIGURED = "notification.message.user.tenant.not.configured";
    public static final String NMESSAGE_USER_CONFIGURED = "notification.message.user.configured";
    public static final String NMESSAGE_SENT_SUCCESS = "notification.message.sent.success";
    
    public static final String NMESSAGE_BAD_PARAM = "notification.message.bad.param";
    
    public static final DateTimeFormatter NOTIF_DATETIME_FORMATTER = DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a");
    public static final DateTimeFormatter NOTIF_DATE_FORMATTER = DateTimeFormat.forPattern("MMM dd, yyyy");
    public static final DateTimeFormatter NOTIF_TIME_FORMATTER = DateTimeFormat.forPattern("hh:mm a");
        
    public static final int NOTIFICATION_OPEN_SHIFT_MAX = 20;

    public static final String EMLOGIS_SERVER_URL ="emlogis.server.url";

    public static final String TRACKING_ENABLED = "tracking.enabled";
    public static final String TRACKING_DATABASE = "tracking.database";
    public static final String TRACKING_STATUSES = "tracking.statuses";

    public static final String AWS_FOLDER_PROPERTY = "aws.folder";
    public final static String AWS_DEFAULT_FOLDER = "MercuryDev/Reports";
    public final static String AWS_PROFILE_CONFIG_FILE_PATH = "awscredentials";
    public final static String AWS_PROFILE_NAME = "default";
    public final static String SCHEDULE_REPORT_AWS_USE = "schedule.report.aws.use";

    public final static String VALIDATION_OBJECT_TYPE = "validation.object.type";
    public final static String VALIDATION_VALIDATOR_FAILED = "validation.validator.failed";

    public static final String WORKFLOW_ACTION_EXECUTION_TIMEOUT = "workflow.action.wait.time";
}
