package com.example.project.entity;

/**
 * Database entity for legacy permission definitions.
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
@Table(name = "permissions", indexes = {
		@Index(name = "idx_permission_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	public enum PermissionName {
		USER_READ,
		USER_WRITE,
		USER_DELETE,
		FILE_UPLOAD,
		FILE_DOWNLOAD,
		REPORT_EXPORT,
		REPORT_IMPORT,
		AUDIT_READ,
		ADMIN_MANAGE
	}
}
