package com.example.project.entity;

/**
 * Database entity for WASAC user roles such as ADMIN and OPERATOR.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles", indexes = {
		@Index(name = "idx_role_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 50)
	private RoleName name;

	public enum RoleName {
		ROLE_ADMIN,
		ROLE_OPERATOR,
		ROLE_FINANCE,
		ROLE_CUSTOMER
	}
}
