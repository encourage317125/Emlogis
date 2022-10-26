package com.emlogis.model.workflow.entities;


import com.emlogis.common.UniqueId;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.workflow.dto.commentary.RequestCommentary;
import com.emlogis.workflow.WflUtil;
import com.emlogis.workflow.enums.AvailabilityRequestSubtype;
import com.emlogis.workflow.enums.WorkflowActionDict;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.status.RequestTechnicalStatusDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jsoup.helper.StringUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.emlogis.common.EmlogisUtils.toJsonString;
import static com.emlogis.workflow.WflUtil.currentDateTime;
import static com.emlogis.workflow.WflUtil.getPeerAggregatedRequestStatus;
import static com.emlogis.workflow.WflUtil.isSwapOrWip;

/**
 * Created by Developer on 19.01.2015.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WorkflowRequest extends BaseEntity implements PkEntity, Serializable {

    @ManyToOne(targetEntity = WflProcess.class, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "fk_wfl_proto_process_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "fk_wfl_proto_process_tenant_id", referencedColumnName = "tenantId", nullable = false)
    })
    private WflProcess protoProcess;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime expiration = new DateTime(0);

//    @Basic
//    @Column(name = "apply_strategy", nullable = false)
//    @Enumerated(EnumType.STRING)
//    private WorkflowPeerWinnerStrategyDict applyStrategy = WorkflowPeerWinnerStrategyDict.MANUAL_SELECT;

    @Lob  // to be decided if we store only the last comment or the full history (see history field)
    @Column(name = "comment", length = 2048)
    private String comment;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime requestDate = new DateTime(0);                // requestDate date

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestTechnicalStatusDict status;

    @Column(name = "requestStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkflowRequestStatusDict requestStatus = WorkflowRequestStatusDict.ADMIN_PENDING;

    // Is this equiv to reviewer ?
    @ManyToOne(targetEntity = Employee.class, optional = false, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumns({@JoinColumn(name = "fk_changer_tenant_id", referencedColumnName = "tenantId"),
            @JoinColumn(name = "fk_changer_employee_id", referencedColumnName = "id")})
    private Employee statusChanger;

    @ManyToOne(targetEntity = Employee.class, optional = false, fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumns({@JoinColumn(name = "fk_initiator_tenant_id", referencedColumnName = "tenantId"),
            @JoinColumn(name = "fk_initiator_employee_id", referencedColumnName = "id")})
    private Employee initiator;

    @Column(name = "engine_id", nullable = true)
    private Long engineId;

    @OneToMany(targetEntity = WorkflowRequestPeer.class,
            mappedBy = "process", orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private Set<WorkflowRequestPeer> recipients;

    @OneToMany(targetEntity = WorkflowRequestLog.class,
            mappedBy = "processInstance",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private Set<WorkflowRequestLog> actions = new HashSet<>();

    @OneToMany(targetEntity = WorkflowRequestManager.class,
            mappedBy = "request",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private Set<WorkflowRequestManager> managers = new HashSet<>();

    @Basic
    @Column(nullable = false)
    private String submitterShiftId = "EMPTY";

    @Basic
    @Column(nullable = true)
    private Long submitterShiftStartDateTime;

    @Basic
    @Column(nullable = true)
    private Long submitterShiftEndDateTime;

    @Basic
    @Column(nullable = true)
    private String submitterShiftSkillId;

    @Basic
    @Column(nullable = true)
    private String submitterShiftSkillName;

    @Column(nullable = false)
    private String code;

    @Column(nullable = true)
    private String chosenPeerId;

    @Column
    @Enumerated(EnumType.STRING)
    private WorkflowRequestTypeDict requestType;

    @Lob
    @Column(length = 1024, nullable = false)
    private String data;                        // json serialized request data (like submitter shift, pto dates, availability data, etc)

    @Lob
    @Column(length = 1024)
    private String managerIds;                    // comma separated list of manager Ids.  should be truncated on a id boundary if too long

    @Column(length = 1024)
    private String managerNames;                // comma separated list of manager Names (first name last name). can be truncated of too long

    // actually submitted should be 'created' from BaseEntity
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime submitted = new DateTime(0);    // request submit date/time UTC 

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime reviewed = new DateTime(0);// request review by manager date/time UTC

    @Column
    private String reviewerId;                    // id of manager Ids who reviewed the request
    @Column
    private String reviewerName;                // first name lastname of manager who reviewed the request

    @Column
    private String declineReason;                // information about decline reason. (employe avail chnaged, manager decision, etc ...)

    @Column
    private Integer peerCount = 0;                // nb of total peers in request (applicable only to WIP/SWAP requests)
    @Column
    private Integer peerApprovedCount = 0;        // nb of peers having currently accepted (applicable only to WIP/SWAP requests)

    @Lob
    @Column
    private byte[] history;                        // list of [timestamp, [action, actorId, actor name, prevReqStatus, newReqStatus, comment]
    // to be defined when this is updated. on each transition, or on transition to a final state and delete of Logs
    @Column
    private String submitterId;                // submitter information.
    @Column
    private String submitterName;
    @Column
    private String submitterTeamId;
    @Column
    private String submitterTeamName;
    @Column
    private String submitterSiteId;
    @Column
    private String submitterSiteName;
    @Column
    private DateTimeZone submitterTz = DateTimeZone.UTC;
    @Column
    private String submitterLang;
    @Column
    private String submitterCountry;

    @Column(name = "availabilityRequestSubtype", nullable = false)
    @Enumerated(EnumType.STRING)
    private AvailabilityRequestSubtype availabilityRequestSubtype = AvailabilityRequestSubtype.NONE;

    @Column(name = "managerDescription", nullable = true)
    private String managerDescription;

    @Column(name = "submitterDescription", nullable = true)
    private String submitterDescription;

    @Column(name = "peerDescription", nullable = true)
    private String peerDescription;

    public WorkflowRequest() {
        super();
    }

    public WorkflowRequest(
            WflProcess protoProcess,
            RequestTechnicalStatusDict status,
            Employee changer,
            Employee submitter,
            Long expiration,
            String submitterShiftId,
            Long submitterShiftStartDateTime,
            Long submitterShiftEndDateTime,
            String submitterShiftSkillId,
            String submitterShiftSkillName,
            String submitterTeamId,
            String submitterTeamName,
            String submitterSiteId,
            String submitterSiteName,
            DateTimeZone submitterTz,
            String submitterCountry,
            String submitterLanguage,
            Long requestDate,
            AvailabilityRequestSubtype availabilityRequestSubtype,
            String data,
            Integer recipientsSize
    ) {
        super(new PrimaryKey(submitter.getTenantId()));
        this.data = data;
        this.peerCount = recipientsSize;
        this.submitted = new DateTime(currentDateTime());
        this.requestType = protoProcess.getType().getType();
        this.availabilityRequestSubtype = availabilityRequestSubtype;
        this.initiator = submitter;
        this.submitterId = submitter.getId();
        this.submitterName = submitter.reportName();
        this.submitterTeamId = submitterTeamId;
        this.submitterTeamName = submitterTeamName;
        this.submitterShiftStartDateTime = submitterShiftStartDateTime;
        this.submitterShiftEndDateTime = submitterShiftEndDateTime;
        this.submitterShiftSkillName = submitterShiftSkillName;
        this.submitterShiftSkillId = submitterShiftSkillId;
        if (submitterShiftId != null) {
            this.submitterShiftId = submitterShiftId;
        }
        this.protoProcess = protoProcess;
        this.setExpiration(expiration);
        this.submitterSiteId = submitterSiteId;
        this.submitterSiteName = submitterSiteName;
        this.submitterTz = submitterTz;
        this.submitterCountry = submitterCountry;
        this.submitterLang = submitterLanguage;
        this.status = status;
        this.statusChanger = changer;
        if (isSwapOrWip(this)) {
            this.setStatus(RequestTechnicalStatusDict.PROCESS_INITIATED);
            this.setRequestStatus(WorkflowRequestStatusDict.PEER_PENDING);
        } else {
            this.setStatus(RequestTechnicalStatusDict.READY_FOR_ADMIN);
            this.setRequestStatus(WorkflowRequestStatusDict.ADMIN_PENDING);
        }
        if (this.code == null) {
            code = UniqueId.getId();
        }
        switch (this.requestType) {
            case AVAILABILITY_REQUEST: {
                this.requestDate = new DateTime(requestDate);
            }
            case TIME_OFF_REQUEST:{
                 this.requestDate = new DateTime(requestDate);
            }
            case OPEN_SHIFT_REQUEST:{
                 this.requestDate = new DateTime(submitterShiftStartDateTime);
            }
            case SHIFT_SWAP_REQUEST:{
                this.requestDate = new DateTime(submitterShiftStartDateTime);
            }
            case WIP_REQUEST:{
                this.requestDate = new DateTime(submitterShiftStartDateTime);
            }
        }
    }

    public String getPeerDescription() {
        return peerDescription;
    }

    public void setPeerDescription(String peerDescription) {
        this.peerDescription = peerDescription;
    }

    public String getManagerDescription() {
        return managerDescription;
    }

    public void setManagerDescription(String managerDescription) {
        this.managerDescription = managerDescription;
    }

    public String getSubmitterDescription() {
        return submitterDescription;
    }

    public void setSubmitterDescription(String submitterDescription) {
        this.submitterDescription = submitterDescription;
    }

    public String commentary() {
        if (comment == null) {
            comment = toJsonString(new RequestCommentary());
        }
        return comment;
    }

    public Set<WorkflowRequestManager> getManagers() {
        if (managers == null) {
            managers = new HashSet<>();
        }
        return managers;
    }

    public void setManagers(Set<WorkflowRequestManager> managers) {
        this.managers = managers;
    }

    public AvailabilityRequestSubtype getAvailabilityRequestSubtype() {
        return availabilityRequestSubtype;
    }

    public void setAvailabilityRequestSubtype(AvailabilityRequestSubtype availabilityRequestSubtype) {
        this.availabilityRequestSubtype = availabilityRequestSubtype;
    }

    public String getChosenPeerId() {
        return chosenPeerId;
    }

    public void setChosenPeerId(String chosenPeerId) {
        this.chosenPeerId = chosenPeerId;
    }

    public WflProcess getProtoProcess() {
        return protoProcess;
    }

    public void setProtoProcess(WflProcess protoProcess) {
        this.protoProcess = protoProcess;
    }

    public Long getExpiration() {
        return new Long(expiration == null ? 0L : expiration.getMillis());
    }

    public void setExpiration(Long expiration) {
        this.expiration = (expiration != null ? new DateTime(expiration) : null);
    }

//    public WorkflowPeerWinnerStrategyDict getApplyStrategy() {
//        return applyStrategy;
//    }
//
//    public void setApplyStrategy(WorkflowPeerWinnerStrategyDict applyStrategy) {
//        this.applyStrategy = applyStrategy;
//    }

    public RequestTechnicalStatusDict getStatus() {
        return status;
    }

    public void setStatus(RequestTechnicalStatusDict status) {
        this.status = status;
    }

    public Long getEngineId() {
        return engineId;
    }

    public void setEngineId(Long engineId) {
        this.engineId = engineId;
    }

    public String getComment() {
        if (StringUtil.isBlank(comment)) {
            this.comment = "COMMENTS: ";
        }
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<WorkflowRequestPeer> getRecipients() {
        if (recipients == null) {
            recipients = new HashSet<>();
        }
        return recipients;
    }

    public void setRecipients(Set<WorkflowRequestPeer> recipients) {
        this.recipients = recipients;
    }

    public Set<WorkflowRequestLog> getActions() {
        if (actions == null) {
            actions = new HashSet<>();
        }
        return actions;
    }

    public void setActions(Set<WorkflowRequestLog> actions) {
        if (actions == null) {
            actions = new HashSet<>();
        }
        this.actions = actions;
    }

    public String getSubmitterShiftId() {
        if (submitterShiftId == null) {
            submitterShiftId = "EMPTY";
        }
        return submitterShiftId;
    }

    public void setSubmitterShiftId(String submitterShiftId) {
        this.submitterShiftId = submitterShiftId;
    }


    public Employee getStatusChanger() {
        return statusChanger;
    }

    public void setStatusChanger(Employee statusChanger) {
        this.statusChanger = statusChanger;
    }

    public Employee getInitiator() {
        return initiator;
    }

    public void setInitiator(Employee initiator) {
        this.initiator = initiator;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String notificationCode) {
        this.code = notificationCode;
    }

    public WorkflowRequestStatusDict getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus) {
        if(requestStatus.equals(WorkflowRequestStatusDict.PEER_DECLINED)) {
            this.status = RequestTechnicalStatusDict.DECLINED_BY_PEERS;
            this.requestStatus = WorkflowRequestStatusDict.DECLINED;
            declineReason = "All peers declined";
            for (WorkflowRequestPeer peer: getRecipients()) {
                peer.setPeerStatus(getPeerAggregatedRequestStatus(this, peer));
            }
        } else {
            this.requestStatus = requestStatus;
        }
    }

    public void setRequestStatus(WorkflowRequestStatusDict requestStatus, String declineReason) {
        this.requestStatus = requestStatus;
        this.declineReason = declineReason;
    }

    public Long getRequestDate() {
        return new Long(requestDate == null ? 0L : requestDate.getMillis());
    }

    public void setRequestDate(Long requestDate) {
        this.requestDate = (requestDate != null ? new DateTime(requestDate) : null);
    }

    public Boolean hasShift() {
        return submitterShiftId != null && !submitterShiftId.equals("EMPTY");
    }

    public WorkflowRequestTypeDict getRequestType() {
        return requestType;
    }

    public void setRequestType(WorkflowRequestTypeDict requestType) {
        this.requestType = requestType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(String managerIds) {
        this.managerIds = managerIds;
    }

    public String getManagerNames() {
        return managerNames;
    }

    public void setManagerNames(String managerNames) {
        this.managerNames = managerNames;
    }

    public long getSubmitted() {
        return submitted == null ? 0 : submitted.getMillis();

    }

    public void setSubmitted(long submitted) {
        this.submitted = new DateTime(submitted);
    }

    public long getReviewed() {
        return reviewed == null ? 0 : reviewed.getMillis();
    }

    public void setReviewed(long reviewed) {
        this.reviewed = new DateTime(reviewed);
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public Integer getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(Integer peerCount) {
        this.peerCount = peerCount;
    }

    public Integer getPeerApprovedCount() {
        return peerApprovedCount;
    }

    public void setPeerApprovedCount(Integer peerApprovedCount) {
        this.peerApprovedCount = peerApprovedCount;
    }

    public byte[] getHistory() {
        return history;
    }

    public void setHistory(byte[] history) {
        this.history = history;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getSubmitterTeamId() {
        return submitterTeamId;
    }

    public void setSubmitterTeamId(String submitterTeamId) {
        this.submitterTeamId = submitterTeamId;
    }

    public String getSubmitterTeamName() {
        return submitterTeamName;
    }

    public void setSubmitterTeamName(String submitterTeamName) {
        this.submitterTeamName = submitterTeamName;
    }

    public String getSubmitterSiteId() {
        return submitterSiteId;
    }

    public void setSubmitterSiteId(String submitterSiteId) {
        this.submitterSiteId = submitterSiteId;
    }

    public String getSubmitterSiteName() {
        return submitterSiteName;
    }

    public void setSubmitterSiteName(String submitterSiteName) {
        this.submitterSiteName = submitterSiteName;
    }

    public DateTimeZone getSubmitterTz() {
        return submitterTz;
    }

    public void setSubmitterTz(DateTimeZone submitterTz) {
        this.submitterTz = submitterTz;
    }

    public String getSubmitterLang() {
        return submitterLang;
    }

    public void setSubmitterLang(String submitterLang) {
        this.submitterLang = submitterLang;
    }

    public String getSubmitterCountry() {
        return submitterCountry;
    }

    public void setSubmitterCountry(String submitterCountry) {
        this.submitterCountry = submitterCountry;
    }

    public Long getSubmitterShiftStartDateTime() {
        return submitterShiftStartDateTime;
    }

    public void setSubmitterShiftStartDateTime(Long submitterShiftStartDateTime) {
        this.submitterShiftStartDateTime = submitterShiftStartDateTime;
    }

    public Long getSubmitterShiftEndDateTime() {
        return submitterShiftEndDateTime;
    }

    public void setSubmitterShiftEndDateTime(Long submitterShiftEndDateTime) {
        this.submitterShiftEndDateTime = submitterShiftEndDateTime;
    }

    public String getSubmitterShiftSkillId() {
        return submitterShiftSkillId;
    }

    public void setSubmitterShiftSkillId(String submitterShiftSkillId) {
        this.submitterShiftSkillId = submitterShiftSkillId;
    }

    public String getSubmitterShiftSkillName() {
        return submitterShiftSkillName;
    }

    public void setSubmitterShiftSkillName(String submitterShiftSkillName) {
        this.submitterShiftSkillName = submitterShiftSkillName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkflowRequest) {
            WorkflowRequest other = (WorkflowRequest) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getInitiator().getId(), other.getInitiator().getId());
            builder.append(getInitiator().getTenantId(), other.getInitiator().getTenantId());
            builder.append(getSubmitterShiftId(), other.getSubmitterShiftId());
            builder.append(getSubmitterTeamId(), other.getSubmitterTeamId());
            builder.append(getAvailabilityRequestSubtype(), other.getAvailabilityRequestSubtype());
            builder.append(getData(), other.getData());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getTenantId());
        builder.append(getInitiator().getId());
        builder.append(getInitiator().getTenantId());
        builder.append(getSubmitterShiftId());
        builder.append(getSubmitterTeamId());
        builder.append(getAvailabilityRequestSubtype());
        builder.append(getData());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("tenant", getInitiator().getTenantId()).
                append("type", getRequestType().name()).
                append("technical_status", getStatus().name()).
                append("request_status", getRequestStatus().name()).
                append("initiator_name", getInitiator().reportName()).
                append("initiator id", getInitiator().getId()).
                append("initiated_date", getCreated()).
                append("submitterShiftId", getSubmitterShiftId()).
                append("teamId", getSubmitterTeamId()).
                toString();
    }

    public Locale locale() {
        String language = getSubmitterLang() != null ? getSubmitterLang() : "en";
        String country = getSubmitterCountry() != null ? getSubmitterCountry() : "US";
        return new Locale(language, country);
    }

    public String getInitiatorComment() {
        for (WorkflowRequestLog action : getActions()) {
            if (action.getAction().equals(WorkflowActionDict.ORIGINATOR_PROCEED)) {
                return action.getComment();
            }
        }
        return new String();
    }

    public Set<WorkflowRequestPeer> getIdlePeersOnEmployee(Employee employee) {
        Set<WorkflowRequestPeer> resultSet = new HashSet<>();
        for (WorkflowRequestPeer peer : getRecipients()) {
            Boolean sameEmployee = peer.getRecipient().getId().equals(employee.getId());
            Boolean acted = false;
            if (sameEmployee) {
                for (WorkflowRequestLog action : getActions()) {
                    if (action.getActorId().equals(employee.getId())) {
                        if (peer.getPeerShiftId().equals(action.getShiftId())) {
                            acted = true;
                        }
                    }
                }
            }
            if (sameEmployee && !acted && !resultSet.contains(peer)) {
                resultSet.add(peer);
            }
        }
        return resultSet;
    }

    public WorkflowActionDict lastAction() {
        WorkflowRequestLog latest = null;
        for (WorkflowRequestLog requestLog : getActions()) {
            if (latest == null) {
                latest = requestLog;
            } else if (latest.getCreated().getMillis() <= requestLog.getCreated().getMillis()) {
                latest = requestLog;
            }
        }
        return latest.getAction();
    }
}
