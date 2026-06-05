package com.example.project.dto.response;

/**
 * API response shape for a tariff version and its tiers.
 */

import com.example.project.entity.enums.MeterType;
import com.example.project.entity.enums.TariffType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TariffResponse {

	private Long id;
	private MeterType meterType;
	private int versionNumber;
	private LocalDate effectiveFrom;
	private BigDecimal fixedServiceCharge;
	private BigDecimal vatRate;
	private BigDecimal latePenaltyRate;
	private TariffType tariffType;
	private boolean tierBased;
	private List<TariffTierResponse> tiers;
	private LocalDateTime createdAt;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TariffTierResponse {
		private Long id;
		private BigDecimal tierFrom;
		private BigDecimal tierTo;
		private BigDecimal ratePerUnit;
	}
}
