package com.example.project.entity;

/**
 * Database entity representing a monthly utility bill.
 */

import com.example.project.entity.enums.BillStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bills", indexes = {
		@Index(name = "idx_bill_reference", columnList = "billReference", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill extends BaseEntity {

	@Column(nullable = false, unique = true, length = 50)
	private String billReference;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meter_id", nullable = false)
	private Meter meter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "billing_cycle_id", nullable = false)
	private BillingCycle billingCycle;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tariff_version_id", nullable = false)
	private TariffVersion tariffVersion;

	@Column(name = "meter_reading_id")
	private Long meterReadingId;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal usageUnits;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal fixedCharge;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal vatAmount;

	@Column(nullable = false, precision = 12, scale = 2)
	@Builder.Default
	private BigDecimal penaltyAmount = BigDecimal.ZERO;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal outstandingAmount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private BillStatus status = BillStatus.PENDING;

	@Column(nullable = false)
	private LocalDate dueDate;
}
