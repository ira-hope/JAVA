package com.example.project.dto.request;

/**
 * Request body for generating bills for a given month and year.
 */

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingGenerateRequest {

	@NotNull(message = "Year is required")
	@Min(value = 2000, message = "Year must be 2000 or later")
	private Integer year;

	@NotNull(message = "Month is required")
	@Min(value = 1, message = "Month must be between 1 and 12")
	@Max(value = 12, message = "Month must be between 1 and 12")
	private Integer month;
}
