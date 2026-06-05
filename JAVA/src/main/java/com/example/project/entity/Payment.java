package com.example.project.entity;

/**
 * Database entity for a payment made against a bill.
 */

import com.example.project.entity.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

	@Column(nullable = false, length = 50)
	private String billReference;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bill_id", nullable = false)
	private Bill bill;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amountPaid;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PaymentMethod paymentMethod;

	@Column(nullable = false)
	private LocalDate paymentDate;
}
