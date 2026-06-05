package com.example.project.entity;

/**
 * Database entity for a monthly meter reading record.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meter_readings", indexes = {
		@Index(name = "idx_reading_meter_period", columnList = "meter_id,readingYear,readingMonth")
}, uniqueConstraints = {
		@UniqueConstraint(name = "uk_meter_reading_period", columnNames = {"meter_id", "readingYear", "readingMonth"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meter_id", nullable = false)
	private Meter meter;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal previousReading;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal currentReading;

	@Column(nullable = false)
	private LocalDate readingDate;

	@Column(nullable = false)
	private int readingYear;

	@Column(nullable = false)
	private int readingMonth;
}
