package com.example.project.entity;

/**
 * Database entity for a WASAC utility customer.
 */

import com.example.project.entity.enums.CustomerStatus;
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
@Table(name = "customers", indexes = {
		@Index(name = "idx_customer_national_id", columnList = "nationalId", unique = true),
		@Index(name = "idx_customer_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false, unique = true, length = 20)
	private String nationalId;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private CustomerStatus status = CustomerStatus.ACTIVE;
}
