package com.emlogis.model.workflow.entities;

import com.emlogis.model.BaseEntity;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.common.PkEntity;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.schedule.Shift;
import com.emlogis.model.structurelevel.Site;
import com.emlogis.model.structurelevel.Team;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.workflow.enums.WorkflowRequestTypeDict;
import com.emlogis.workflow.enums.status.WorkflowRequestStatusDict;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static com.emlogis.workflow.WflUtil.isSwapOrWip;

/**
 * Created by alexborlis on 22.01.15.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class WorkflowRequestPeer extends BaseEntity implements PkEntity, Serializable {

    @ManyToOne(targetEntity = WorkflowRequest.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "fk_wfl_process_instance_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "fk_wfl_process_tenant_id", referencedColumnName = "tenantId", nullable = false)
    })
    private WorkflowRequest process;

    @ManyToOne(targetEntity = Employee.class, optional = false, fetch = FetchType.EAGER)
    @JoinColumns({
            @JoinColumn(name = "fk_recipient_tenant_id", referencedColumnName = "tenantId"),
            @JoinColumn(name = "fk_recipient_employee_id", referencedColumnName = "id")
    })
    private Employee recipient;

    @Basic
    @Column(name = "selected_manually", nullable = false)
    private Boolean selectedManually;

    @Column(name = "peerStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkflowRequestStatusDict peerStatus = WorkflowRequestStatusDict.PEER_PENDING;


    // NEW ATTRIBUTES FOR OPTIMIZING QUERIES
    @Column
    @Enumerated(EnumType.STRING)
    WorkflowRequestTypeDict requestType;
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime responded = new DateTime(0);// date the peer has approved or declined the request date/time UTC
    @Column
    private String declineReason;    // information about decline reason. (employe avail changed, manager decision, etc ...)

    @Column
    private String peerId;                        // Peer information.
    @Column
    private String peerName;
    @Column
    private String peerTeamId;
    @Column
    private String peerTeamName;
    @Column
    private String peerSiteId;
    @Column
    private String peerSiteName;
    @Column
    private DateTimeZone peerTz = DateTimeZone.UTC;
    @Column
    private String peerLang;
    @Column
    private String peerCountry;

    @Column(nullable = false)
    private String peerShiftId;                // Peer Shift information.
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime peerShiftStartDateTime = new DateTime(0);// request review by manager date/time UTC
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime peerShiftEndDateTime = new DateTime(0);// request review by manager date/time UTC
    @Column
    private String peerShiftTeamId;
    @Column
    private String peerShiftTeamName;
    @Column
    private String peerShiftSkillId;
    @Column
    private String peerShiftSkillName;


    @Column
    private String submitterId;
    @Column
    private String submitterName;

    @Column
    private String submitterShiftId;            // Submitter Shift information.
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime submitterShiftStartDateTime = new DateTime(0);// request review by manager date/time UTC
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime submitterShiftEndDateTime = new DateTime(0);// request review by manager date/time UTC
    @Column
    private String submitterShiftTeamId;
    @Column
    private String submitterShiftTeamName;
    @Column
    private String submitterShiftSkillId;
    @Column
    private String submitterShiftSkillName;

    @Column(nullable = false)
    private Boolean isRead;

    public WorkflowRequestPeer() {
        super();
        this.selectedManually = false;
        this.peerShiftId = "EMPTY";
        this.isRead = false;
    }

    public WorkflowRequestPeer(
            WorkflowRequest request,
            UserAccount userAccount,
            Employee peerEmployee,
            Team peerTeam,
            Site peerSite,
            Shift peerShift
    ) {
        super(new PrimaryKey(request.getTenantId()));
        this.process = request;
        this.recipient = peerEmployee;
        this.isRead = false;
        this.selectedManually = false;
        this.requestType = request.getRequestType();
        this.peerId = peerEmployee.getId();
        this.peerName = peerEmployee.reportName();
        this.peerTeamId = peerTeam.getId();
        this.peerTeamName = peerTeam.getName();
        this.peerSiteId = peerSite.getId();
        this.peerSiteName = peerSite.getName();
        this.peerTz = userAccount.getTimeZone() != null ? userAccount.getTimeZone() : peerSite.getTimeZone();
        this.peerLang = userAccount.getLanguage() != null ? userAccount.getLanguage() : peerSite.getLanguage();
        this.peerCountry = userAccount.getCountry() != null ? userAccount.getCountry() : peerSite.getCountry();
        this.peerShiftId = "EMPTY";
        if (isSwapOrWip(request)) {
            this.setPeerStatus(WorkflowRequestStatusDict.PEER_PENDING);
        } else {
            this.setPeerStatus(WorkflowRequestStatusDict.DECLINED);
        }
        if (peerShift != null) {
            this.peerShiftId = peerShift.getId();
            this.peerShiftStartDateTime = new DateTime(peerShift.getStartDateTime());
            this.peerShiftEndDateTime = new DateTime(peerShift.getEndDateTime());
            this.peerShiftTeamId = peerShift.getTeamId();
            this.peerShiftTeamName = peerShift.getTeamName();
            this.peerShiftSkillId = peerShift.getSkillId();
            this.peerShiftSkillName = peerShift.getSkillName();
        }
        this.submitterId = request.getSubmitterId();
        this.submitterName = request.getSubmitterName();
        this.submitterShiftId = request.getSubmitterShiftId();
        this.submitterShiftStartDateTime = new DateTime(request.getSubmitterShiftStartDateTime());
        this.submitterShiftEndDateTime = new DateTime(request.getSubmitterShiftEndDateTime());
        this.submitterShiftTeamId = request.getSubmitterTeamId();
        this.submitterShiftTeamName = request.getSubmitterTeamName();
        this.submitterShiftSkillId = request.getSubmitterShiftSkillId();
        this.submitterShiftSkillName = request.getSubmitterShiftSkillName();
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

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getSelectedManually() {
        return selectedManually;
    }

    public void setSelectedManually(Boolean selectedManually) {
        this.selectedManually = selectedManually;
    }

    public Employee getRecipient() {
        return recipient;
    }

    public void setRecipient(Employee recipient) {
        this.recipient = recipient;
    }

    public Boolean isSelectedManually() {
        return selectedManually;
    }

    public WorkflowRequest getProcess() {
        return process;
    }

    public void setProcess(WorkflowRequest processInstance) {
        this.process = processInstance;
    }

    public WorkflowRequestStatusDict getPeerStatus() {
        return peerStatus;
    }

    public void setPeerStatus(WorkflowRequestStatusDict peerStatus) {
        this.peerStatus = peerStatus;

        switch (peerStatus) {
            case UNKNOWN:    //  failure:
                this.setDeclineReason("Internal Error, conditions led to an unexpected state for Peer");
                break;
            case PEER_PENDING:
                break;
            case PEER_APPROVED:
                break;
            case APPROVED:
                break;
            case PEER_DECLINED:    // TODO check this is when all peers declined
                this.setDeclineReason("Peer decision");
                break;
            case DECLINED:
                this.setDeclineReason("Manager decision");
                break;
            case WITHDRAWN:
                break;
            case EXPIRED:
                break;
        }
    }

    public WorkflowRequestTypeDict getRequestType() {
        return requestType;
    }

    public void setRequestType(WorkflowRequestTypeDict requestType) {
        this.requestType = requestType;
    }

    public long getResponded() {
        return responded == null ? 0 : responded.getMillis();
    }

    public void setResponded(long responded) {
        this.responded = new DateTime(responded);
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerTeamId() {
        return peerTeamId;
    }

    public void setPeerTeamId(String peerTeamId) {
        this.peerTeamId = peerTeamId;
    }

    public String getPeerTeamName() {
        return peerTeamName;
    }

    public void setPeerTeamName(String peerTeamName) {
        this.peerTeamName = peerTeamName;
    }

    public String getPeerSiteId() {
        return peerSiteId;
    }

    public void setPeerSiteId(String peerSiteId) {
        this.peerSiteId = peerSiteId;
    }

    public String getPeerSiteName() {
        return peerSiteName;
    }

    public void setPeerSiteName(String peerSiteName) {
        this.peerSiteName = peerSiteName;
    }

    public DateTimeZone getPeerTz() {
        return peerTz;
    }

    public void setPeerTz(DateTimeZone peerTz) {
        this.peerTz = peerTz;
    }

    public String getPeerLang() {
        return peerLang;
    }

    public void setPeerLang(String peerLang) {
        this.peerLang = peerLang;
    }

    public String getPeerCountry() {
        return peerCountry;
    }

    public void setPeerCountry(String peerCountry) {
        this.peerCountry = peerCountry;
    }

    public String getPeerShiftId() {
        return peerShiftId;
    }

    public void setPeerShiftId(String peerShiftId) {
        if (peerShiftId != null) {
            this.peerShiftId = peerShiftId;
        }
    }

    public long getPeerShiftStartDateTime() {
        return peerShiftStartDateTime == null ? 0 : peerShiftStartDateTime.getMillis();

    }

    public void setPeerShiftStartDateTime(long peerShiftStartDateTime) {
        this.peerShiftStartDateTime = new DateTime(peerShiftStartDateTime);
    }

    public long getPeerShiftEndDateTime() {
        return peerShiftEndDateTime == null ? 0 : peerShiftEndDateTime.getMillis();

    }

    public void setPeerShiftEndDateTime(long peerShiftEndDateTime) {
        this.peerShiftEndDateTime = new DateTime(peerShiftEndDateTime);
    }

    public String getPeerShiftTeamId() {
        return peerShiftTeamId;
    }

    public void setPeerShiftTeamId(String peerShiftTeamId) {
        this.peerShiftTeamId = peerShiftTeamId;
    }

    public String getPeerShiftTeamName() {
        return peerShiftTeamName;
    }

    public void setPeerShiftTeamName(String peerShiftTeamName) {
        this.peerShiftTeamName = peerShiftTeamName;
    }

    public String getPeerShiftSkillId() {
        return peerShiftSkillId;
    }

    public void setPeerShiftSkillId(String peerShiftSkillId) {
        this.peerShiftSkillId = peerShiftSkillId;
    }

    public String getPeerShiftSkillName() {
        return peerShiftSkillName;
    }

    public void setPeerShiftSkillName(String peerShiftSkillName) {
        this.peerShiftSkillName = peerShiftSkillName;
    }

    public String getSubmitterShiftId() {
        return submitterShiftId;
    }

    public void setSubmitterShiftId(String submitterShiftId) {
        this.submitterShiftId = submitterShiftId;
    }

    public long getSubmitterShiftStartDateTime() {
        return submitterShiftStartDateTime == null ? 0 : submitterShiftStartDateTime.getMillis();

    }

    public void setSubmitterShiftStartDateTime(long submitterShiftStartDateTime) {
        this.submitterShiftStartDateTime = new DateTime(submitterShiftStartDateTime);
    }

    public long getSubmitterShiftEndDateTime() {
        return submitterShiftEndDateTime == null ? 0 : submitterShiftEndDateTime.getMillis();

    }

    public void setSubmitterShiftEndDateTime(long submitterShiftEndDateTime) {
        this.submitterShiftEndDateTime = new DateTime(submitterShiftEndDateTime);
    }

    public String getSubmitterShiftTeamId() {
        return submitterShiftTeamId;
    }

    public void setSubmitterShiftTeamId(String submitterShiftTeamId) {
        this.submitterShiftTeamId = submitterShiftTeamId;
    }

    public String getSubmitterShiftTeamName() {
        return submitterShiftTeamName;
    }

    public void setSubmitterShiftTeamName(String submitterShiftTeamName) {
        this.submitterShiftTeamName = submitterShiftTeamName;
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
        if (obj instanceof WorkflowRequestPeer) {
            WorkflowRequestPeer other = (WorkflowRequestPeer) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(getId(), other.getId());
            builder.append(getTenantId(), other.getTenantId());
            builder.append(getProcess().getId(), other.getProcess().getId());
            builder.append(getProcess().getTenantId(), other.getProcess().getTenantId());
            builder.append(getRecipient().getId(), other.getRecipient().getId());
            builder.append(getRecipient().getTenantId(), other.getRecipient().getTenantId());
            builder.append(getPeerShiftId(), other.getPeerShiftId());
            return builder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getId());
        builder.append(getTenantId());
//        builder.append(getProcess().getId());
//        builder.append(getProcess().getTenantId());
//        builder.append(getRecipient().getId());
//        builder.append(getRecipient().getTenantId());
        builder.append(getPeerShiftId());
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", getId()).
                append("tenantId", getTenantId()).
                toString();
    }

    public String commentOnAction() {
        for (WorkflowRequestLog action : getProcess().getActions()) {
            if (action.getActorId().equals(getRecipient().getId())) {
                return action.getComment();
            }
        }
        return null;
    }

    public Long dateActed() {
        for (WorkflowRequestLog action : getProcess().getActions()) {
            if (action.getActorId().equals(getRecipient().getId())) {
                return action.getCreated().getMillis();
            }
        }
        return null;
    }

    public Boolean getDeclinedByManager() {
        return peerStatus == WorkflowRequestStatusDict.DECLINED;
    }

    public boolean hasShift() {
        return this.peerShiftId != null && !this.peerShiftId.equals("EMPTY");
    }
}
