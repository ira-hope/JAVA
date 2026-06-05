package com.example.project.entity;

/**
 * Database entity that stores who did what and when in the system.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs", indexes = {
		@Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
		@Index(name = "idx_audit_performed_by", columnList = "performedBy")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

	@Column(nullable = false)
	private String action;

	private String entityType;

	private Long entityId;

	private String performedBy;

	@Column(length = 2000)
	private String details;
}
