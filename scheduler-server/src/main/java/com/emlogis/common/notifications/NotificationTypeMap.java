package com.emlogis.common.notifications;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

public class NotificationTypeMap {
	private static Map<TypeKey, NotificationType> notificationTypeMap = new HashMap<TypeKey, NotificationType>();
	
	static {

		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.SHIFT_ASSIGN, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);
		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.SHIFT_DROP, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);
		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.SHIFT_ADD, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);
		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.SHIFT_STARTSTOP_MODIFIED, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);
		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.WORK_IN_PLACE, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);
		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.SHIFT_SWAP, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);
		notificationTypeMap.put(new TypeKey(NotificationOperation.SCHEDULE_CHANGE, NotificationCategory.SHIFT_WIP, NotificationRole.NONE, false),
				NotificationType.MY_SCHEDULE_CHANGE);

		notificationTypeMap.put(new TypeKey(NotificationOperation.POST, NotificationCategory.SCHEDULE, NotificationRole.NONE, false),
				NotificationType.SCHEDULE_POSTED);

		notificationTypeMap.put(new TypeKey(NotificationOperation.DELETE, NotificationCategory.SCHEDULE, NotificationRole.NONE, false),
				NotificationType.SCHEDULE_DELETED);

		notificationTypeMap.put(new TypeKey(NotificationOperation.POST, NotificationCategory.OPEN_SHIFTS, NotificationRole.NONE, false),
				NotificationType.OPEN_SHIFT_POSTED);

		notificationTypeMap.put(new TypeKey(NotificationOperation.GENERATION_START, NotificationCategory.SCHEDULE, NotificationRole.NONE, false),
				NotificationType.SCHEDULE_MANAGEMENT);
		notificationTypeMap.put(new TypeKey(NotificationOperation.GENERATION_COMPLETE, NotificationCategory.SCHEDULE, NotificationRole.NONE, false),
				NotificationType.SCHEDULE_MANAGEMENT);
		notificationTypeMap.put(new TypeKey(NotificationOperation.DELETE, NotificationCategory.SCHEDULE, NotificationRole.NONE, false),
				NotificationType.SCHEDULE_MANAGEMENT);


		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.TIME_OFF,
						NotificationRole.SUBMITTER, true),
				NotificationType.TIME_OFF_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.TIME_OFF,
						NotificationRole.MANAGER, true),
				NotificationType.TIME_OFF_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.TIME_OFF,
						NotificationRole.SUBMITTER, true),
				NotificationType.TIME_OFF_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.TIME_OFF,
						NotificationRole.MANAGER, true),
				NotificationType.TIME_OFF_REQUEST);

		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.OPEN_SHIFTS,
						NotificationRole.SUBMITTER, true),
				NotificationType.OPEN_SHIFT_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.OPEN_SHIFTS,
						NotificationRole.MANAGER, true),
				NotificationType.OPEN_SHIFT_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.OPEN_SHIFTS,
						NotificationRole.SUBMITTER, true),
				NotificationType.OPEN_SHIFT_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.OPEN_SHIFTS,
						NotificationRole.MANAGER, true),
				NotificationType.OPEN_SHIFT_REQUEST);


		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.AVAILABILITY,
						NotificationRole.SUBMITTER, true),
				NotificationType.AVAILABILITY_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.AVAILABILITY,
						NotificationRole.MANAGER, true),
				NotificationType.AVAILABILITY_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.AVAILABILITY,
						NotificationRole.SUBMITTER, true),
				NotificationType.AVAILABILITY_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.AVAILABILITY,
						NotificationRole.MANAGER, true),
				NotificationType.AVAILABILITY_REQUEST);


		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_PEER_PENDING,
						NotificationCategory.SHIFT_SWAP,
						NotificationRole.SUBMITTER, true),
				NotificationType.SWAP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_PEER_PENDING,
						NotificationCategory.SHIFT_SWAP,
						NotificationRole.PEER, true),
				NotificationType.SWAP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.SHIFT_SWAP,
						NotificationRole.MANAGER, true),
				NotificationType.SWAP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.SHIFT_SWAP,
						NotificationRole.SUBMITTER, true),
				NotificationType.SWAP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.SHIFT_SWAP,
						NotificationRole.PEER, true),
				NotificationType.SWAP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.SHIFT_SWAP,
						NotificationRole.MANAGER, true),
				NotificationType.SWAP_REQUEST);

		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_PEER_PENDING,
						NotificationCategory.WORK_IN_PLACE,
						NotificationRole.SUBMITTER, true),
				NotificationType.WIP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_PEER_PENDING,
						NotificationCategory.WORK_IN_PLACE,
						NotificationRole.PEER, true),
				NotificationType.WIP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.BECOME_ADMIN_PENDING,
						NotificationCategory.WORK_IN_PLACE,
						NotificationRole.MANAGER, true),
				NotificationType.WIP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.WORK_IN_PLACE,
						NotificationRole.SUBMITTER, true),
				NotificationType.WIP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.WORK_IN_PLACE,
						NotificationRole.PEER, true),
				NotificationType.WIP_REQUEST);
		notificationTypeMap.put(new TypeKey(
						NotificationOperation.OBTAINED_FINAL_STATE,
						NotificationCategory.WORK_IN_PLACE,
						NotificationRole.MANAGER, true),
				NotificationType.WIP_REQUEST);
	}
	
	public static NotificationType getNotificationType(
			NotificationOperation notificationOperation,
			NotificationCategory notificationCategory,
			NotificationRole notificationRole,
			Boolean isWorkflowType) {
		return notificationTypeMap.get(new TypeKey(notificationOperation, notificationCategory, notificationRole, isWorkflowType));
	}

}

class TypeKey {
	private final NotificationCategory notificationCategory;
	private final NotificationOperation notificationOperation;
	private final NotificationRole notificationRole;
	private final Boolean isWorkflowType;
	
	TypeKey(
			NotificationOperation notificationOperation,
			NotificationCategory notificationCategory,
			NotificationRole notificationRole,
			Boolean isWorkflowType
	){
		this.notificationCategory = notificationCategory;
		this.notificationOperation = notificationOperation;
		this.notificationRole = notificationRole;
		this.isWorkflowType = isWorkflowType;
	}

	public NotificationCategory getNotificationCategory() {
		return notificationCategory;
	}

	public NotificationOperation getNotificationOperation() {
		return notificationOperation;
	}

	public NotificationRole getNotificationRole() {
		return notificationRole;
	}

	public Boolean isWorkflowType() {
		return isWorkflowType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeKey) {
			TypeKey other = (TypeKey) obj;
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(getNotificationCategory(), other.getNotificationCategory());
			builder.append(getNotificationOperation(), other.getNotificationOperation());
			builder.append(getNotificationRole(), other.getNotificationRole());
			builder.append(isWorkflowType(), other.isWorkflowType());
			return builder.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(getNotificationCategory());
		builder.append(getNotificationOperation());
		builder.append(getNotificationRole());
		builder.append(isWorkflowType());
		return builder.toHashCode();
	}
}
