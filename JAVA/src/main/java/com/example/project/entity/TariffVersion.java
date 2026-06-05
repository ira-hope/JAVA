package com.example.project.entity;

/**
 * Database entity for a versioned tariff with rates and charges.
 */

import com.example.project.entity.enums.MeterType;
import com.example.project.entity.enums.TariffType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tariff_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffVersion extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MeterType meterType;

	@Column(nullable = false)
	private int versionNumber;

	@Column(nullable = false)
	private LocalDate effectiveFrom;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal fixedServiceCharge;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal vatRate;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal latePenaltyRate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private TariffType tariffType = TariffType.FLAT;

	@Column(nullable = false)
	@Builder.Default
	private boolean tierBased = false;

	@OneToMany(mappedBy = "tariffVersion", fetch = FetchType.LAZY)
	@Builder.Default
	private List<TariffTier> tiers = new ArrayList<>();
}
