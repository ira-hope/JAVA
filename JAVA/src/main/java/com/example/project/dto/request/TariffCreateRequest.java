package com.example.project.dto.request;

/**
 * Request body for creating a new versioned tariff plan.
 */

import com.example.project.entity.enums.MeterType;
import com.example.project.entity.enums.TariffType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TariffCreateRequest {

	@NotNull(message = "Meter type is required")
	private MeterType meterType;

	@NotNull(message = "Effective from date is required")
	private LocalDate effectiveFrom;

	@NotNull(message = "Fixed service charge is required")
	@DecimalMin(value = "0.0", message = "Fixed service charge must be non-negative")
	private BigDecimal fixedServiceCharge;

	@NotNull(message = "VAT rate is required")
	@DecimalMin(value = "0.0", message = "VAT rate must be non-negative")
	private BigDecimal vatRate;

	@NotNull(message = "Late penalty rate is required")
	@DecimalMin(value = "0.0", message = "Late penalty rate must be non-negative")
	private BigDecimal latePenaltyRate;

	private TariffType tariffType;

	@NotNull(message = "Tier-based flag is required")
	private Boolean tierBased;

	@DecimalMin(value = "0.0", inclusive = false, message = "Flat rate must be greater than zero")
	private BigDecimal flatRatePerUnit;

	@Valid
	private List<TariffTierRequest> tiers;

	@Getter
	@Setter
	public static class TariffTierRequest {

		@NotNull
		@DecimalMin(value = "0.0")
		private BigDecimal tierFrom;

		@NotNull
		@DecimalMin(value = "0.0")
		private BigDecimal tierTo;

		@NotNull
		@DecimalMin(value = "0.0", inclusive = false)
		private BigDecimal ratePerUnit;
	}
}
