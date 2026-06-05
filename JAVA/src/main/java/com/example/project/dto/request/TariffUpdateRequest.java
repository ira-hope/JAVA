package com.example.project.dto.request;

/**
 * Request body for updating an existing tariff version.
 */

import com.example.project.entity.enums.MeterType;
import com.example.project.entity.enums.TariffType;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TariffUpdateRequest {

	private MeterType meterType;
	private LocalDate effectiveFrom;
	private TariffType tariffType;

	@DecimalMin(value = "0.0", message = "Fixed service charge must be non-negative")
	private BigDecimal fixedServiceCharge;

	@DecimalMin(value = "0.0", message = "VAT rate must be non-negative")
	private BigDecimal vatRate;

	@DecimalMin(value = "0.0", message = "Late penalty rate must be non-negative")
	private BigDecimal latePenaltyRate;

	@DecimalMin(value = "0.0", inclusive = false, message = "Flat rate must be greater than zero")
	private BigDecimal flatRatePerUnit;

	private List<TariffCreateRequest.TariffTierRequest> tiers;
}
