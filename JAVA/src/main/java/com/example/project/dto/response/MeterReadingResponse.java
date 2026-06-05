package com.example.project.dto.response;

/**
 * API response shape for a captured meter reading.
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingResponse {

	private Long id;
	private Long meterId;
	private String meterNumber;
	private BigDecimal previousReading;
	private BigDecimal currentReading;
	private BigDecimal usageUnits;
	private LocalDate readingDate;
	private int readingYear;
	private int readingMonth;
	private LocalDateTime createdAt;
}
