package com.example.project.entity;

/**
 * Database entity for one pricing tier inside a tariff version.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tariff_version_id", nullable = false)
	private TariffVersion tariffVersion;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal tierFrom;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal tierTo;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal ratePerUnit;
}
