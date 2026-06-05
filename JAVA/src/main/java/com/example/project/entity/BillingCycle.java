package com.example.project.entity;

/**
 * Database entity for a billing month used when generating bills.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "billing_cycles", uniqueConstraints = {
		@UniqueConstraint(name = "uk_billing_cycle_period", columnNames = {"billingYear", "billingMonth"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingCycle extends BaseEntity {

	@Column(nullable = false)
	private int billingYear;

	@Column(nullable = false)
	private int billingMonth;

	@Column(nullable = false)
	private LocalDate cycleStart;

	@Column(nullable = false)
	private LocalDate cycleEnd;
}
