package com.example.project.entity;

/**
 * Database entity for a water or electricity meter linked to a customer.
 */

import com.example.project.entity.enums.MeterStatus;
import com.example.project.entity.enums.MeterType;
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

import java.time.LocalDate;

@Entity
@Table(name = "meters", indexes = {
		@Index(name = "idx_meter_number", columnList = "meterNumber", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meter extends BaseEntity {

	@Column(nullable = false, unique = true, length = 50)
	private String meterNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MeterType meterType;

	@Column(nullable = false)
	private LocalDate installationDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private MeterStatus status = MeterStatus.ACTIVE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;
}
