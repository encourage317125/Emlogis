package com.emlogis.common.services.workflow.process.expire;

import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;

import static com.emlogis.common.Constants.EMLOGIS_PERSISTENCE_UNIT_NAME;
import static com.emlogis.common.EmlogisUtils.deserializeObject;
import static com.emlogis.workflow.WflUtil.asCurrentTime;

/**
 * Created by user on 14.07.15.
 */
@Stateless
@Local(WflExpirationService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class WflExpirationServiceImpl implements WflExpirationService {

    @PersistenceContext(unitName = EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    @Override
    public void expire() {
        //first off all we need to identify current time in submitter tenant time zone
        String organizationsWithTenantQuery =
                new String("SELECT DISTINCT inst.fk_initiator_tenant_id, inst.submitterTz FROM WorkflowRequest inst;");
        Query query = em.createNativeQuery(organizationsWithTenantQuery);
        Collection<Object[]> resultSet = query.getResultList();

        //then select
        for (Object[] resultItem : resultSet) {
            String tenantId = (String) resultItem[0];
            DateTimeZone dtx = (DateTimeZone) deserializeObject((byte[]) resultItem[1]);

            String updateString =
                    new String("UPDATE WorkflowRequest AS instance, WorkflowRequestPeer AS peer " +
                            "      SET instance.status = 'EXPIRED', instance.requestStatus = 'EXPIRED', peer.peerStatus = 'EXPIRED' " +
                            "    WHERE instance.fk_initiator_tenant_id = '" + tenantId + "' " +
                            "      AND instance.expiration < '" + asCurrentTime(dtx) + "' " +
                            "      AND peer.fk_wfl_process_instance_id = instance.id " +
                            "      AND peer.fk_recipient_tenant_id = '" + tenantId + "';");

            em.createNativeQuery(updateString).executeUpdate();
        }
    }

    private class OrganizationWithTimeZone {

        private String tenantId;
        private DateTimeZone submitterTz = DateTimeZone.UTC;

        public OrganizationWithTimeZone(String tenantId, DateTimeZone submitterTz) {
            this.tenantId = tenantId;
            this.submitterTz = submitterTz;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public DateTimeZone getSubmitterTz() {
            return submitterTz;
        }

        public void setSubmitterTz(DateTimeZone submitterTz) {
            this.submitterTz = submitterTz;
        }
    }
}
