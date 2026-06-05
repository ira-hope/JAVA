package com.example.project.dto.request;

/**
 * Request body for capturing a new meter reading.
 */

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MeterReadingCreateRequest {

	@NotNull(message = "Meter ID is required")
	private Long meterId;

	@NotNull(message = "Current reading is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Current reading must be greater than zero")
	private BigDecimal currentReading;

	@NotNull(message = "Reading date is required")
	private LocalDate readingDate;
}
