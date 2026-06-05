package com.example.project.entity;

/**
 * Database entity for system users who log into WASAC.
 */

import com.example.project.entity.enums.RequestedRole;
import com.example.project.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
		@Index(name = "idx_user_email", columnList = "email", unique = true),
		@Index(name = "idx_user_phone", columnList = "phone", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false, unique = true, length = 20)
	private String phone;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private UserStatus status = UserStatus.ACTIVE;

	@Column(nullable = false)
	@Builder.Default
	private boolean emailVerified = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	@Builder.Default
	private RequestedRole requestedRole = RequestedRole.ROLE_CUSTOMER;

	@Column(nullable = false)
	@Builder.Default
	private boolean adminApproved = false;

	@Column(nullable = false)
	@Builder.Default
	private int failedLoginAttempts = 0;

	@Column(nullable = false)
	@Builder.Default
	private boolean accountLocked = false;

	private LocalDateTime accountLockedUntil;

	@Column(name = "customer_id")
	private Long customerId;

	@Column(nullable = false)
	@Builder.Default
	private boolean passwordSet = true;

	@BatchSize(size = 16)
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	@Builder.Default
	private Set<Role> roles = new HashSet<>();
}
