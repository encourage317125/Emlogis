package com.emlogis.rest.auditing;

/**
 * A simple utility class used by the SessionService to hold and communicate the AuditRecord prepared by the SecurityFilter,
 * and used by the AuditInterceptor. Uses a threadlocal variable as implementation.
 * @author EmLogis
 *
 */
public class AuditContext {

	private static ThreadLocal<AuditRecord> localAuditRecord = new ThreadLocal<>();
	
	public AuditContext(AuditRecord auditRecord) {
		localAuditRecord.set(auditRecord);
	}

	public void set(AuditRecord auditRecord) {
		localAuditRecord.set(auditRecord);
	}

	public static AuditRecord get() {
		return localAuditRecord.get();
	}
		
}

