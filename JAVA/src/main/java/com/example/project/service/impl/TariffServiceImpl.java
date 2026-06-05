package com.example.project.service.impl;

/**
 * Implements versioned tariff creation, update, and lookup logic.
 */

import com.example.project.dto.request.TariffCreateRequest;
import com.example.project.dto.request.TariffUpdateRequest;
import com.example.project.dto.response.TariffResponse;
import com.example.project.entity.TariffTier;
import com.example.project.entity.TariffVersion;
import com.example.project.entity.enums.MeterType;
import com.example.project.entity.enums.TariffType;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.TariffTierRepository;
import com.example.project.repository.TariffVersionRepository;
import com.example.project.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

	private static final BigDecimal FLAT_TIER_MAX = new BigDecimal("999999999");

	private final TariffVersionRepository tariffVersionRepository;
	private final TariffTierRepository tariffTierRepository;

	@Override
	@Transactional(readOnly = true)
	public List<TariffResponse> findAll() {
		return tariffVersionRepository.findAll().stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<TariffResponse> findByMeterType(MeterType meterType) {
		return tariffVersionRepository.findByMeterTypeOrderByVersionNumberDesc(meterType).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public TariffResponse findActiveByMeterType(MeterType meterType) {
		TariffVersion tariff = tariffVersionRepository
				.findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionNumberDesc(
						meterType, LocalDate.now())
				.orElseThrow(() -> new ResourceNotFoundException("No active tariff for meter type: " + meterType));
		return toResponse(tariff);
	}

	@Override
	@Transactional(readOnly = true)
	public TariffResponse findById(Long id) {
		return toResponse(getTariff(id));
	}

	@Override
	@Transactional
	public TariffResponse create(TariffCreateRequest request) {
		boolean tierBased = resolveTierBased(request);
		TariffType tariffType = resolveTariffType(request, tierBased);
		List<TariffCreateRequest.TariffTierRequest> tierRequests = resolveTierRequests(request, tierBased);

		int nextVersion = tariffVersionRepository
				.findByMeterTypeOrderByVersionNumberDesc(request.getMeterType())
				.stream()
				.mapToInt(TariffVersion::getVersionNumber)
				.max()
				.orElse(0) + 1;

		TariffVersion tariff = TariffVersion.builder()
				.meterType(request.getMeterType())
				.versionNumber(nextVersion)
				.effectiveFrom(request.getEffectiveFrom())
				.fixedServiceCharge(request.getFixedServiceCharge())
				.vatRate(request.getVatRate())
				.latePenaltyRate(request.getLatePenaltyRate())
				.tariffType(tariffType)
				.tierBased(tierBased)
				.build();

		tariff = tariffVersionRepository.save(tariff);
		saveTiers(tariff, tierRequests);
		return toResponse(tariff);
	}

	@Override
	@Transactional
	public TariffResponse update(Long id, TariffUpdateRequest request) {
		TariffVersion tariff = getTariff(id);
		if (request.getMeterType() != null) {
			tariff.setMeterType(request.getMeterType());
		}
		if (request.getEffectiveFrom() != null) {
			tariff.setEffectiveFrom(request.getEffectiveFrom());
		}
		if (request.getFixedServiceCharge() != null) {
			tariff.setFixedServiceCharge(request.getFixedServiceCharge());
		}
		if (request.getVatRate() != null) {
			tariff.setVatRate(request.getVatRate());
		}
		if (request.getLatePenaltyRate() != null) {
			tariff.setLatePenaltyRate(request.getLatePenaltyRate());
		}
		if (request.getTariffType() != null) {
			tariff.setTariffType(request.getTariffType());
			tariff.setTierBased(request.getTariffType() == TariffType.TIERED);
		}
		if (request.getFlatRatePerUnit() != null || request.getTiers() != null) {
			tariffTierRepository.deleteAll(tariffTierRepository.findByTariffVersionIdOrderByTierFromAsc(tariff.getId()));
			boolean tierBased = tariff.isTierBased();
			TariffCreateRequest createRequest = new TariffCreateRequest();
			createRequest.setFlatRatePerUnit(request.getFlatRatePerUnit());
			createRequest.setTiers(request.getTiers());
			saveTiers(tariff, resolveTierRequests(createRequest, tierBased));
		}
		return toResponse(tariffVersionRepository.save(tariff));
	}

	private void saveTiers(TariffVersion tariff, List<TariffCreateRequest.TariffTierRequest> tierRequests) {
		for (TariffCreateRequest.TariffTierRequest tierReq : tierRequests) {
			tariffTierRepository.save(TariffTier.builder()
					.tariffVersion(tariff)
					.tierFrom(tierReq.getTierFrom())
					.tierTo(tierReq.getTierTo())
					.ratePerUnit(tierReq.getRatePerUnit())
					.build());
		}
	}

	private boolean resolveTierBased(TariffCreateRequest request) {
		if (request.getTariffType() != null) {
			return request.getTariffType() == TariffType.TIERED;
		}
		return Boolean.TRUE.equals(request.getTierBased());
	}

	private TariffType resolveTariffType(TariffCreateRequest request, boolean tierBased) {
		if (request.getTariffType() != null) {
			return request.getTariffType();
		}
		return tierBased ? TariffType.TIERED : TariffType.FLAT;
	}

	private List<TariffCreateRequest.TariffTierRequest> resolveTierRequests(
			TariffCreateRequest request, boolean tierBased) {
		if (tierBased) {
			if (request.getTiers() == null || request.getTiers().isEmpty()) {
				throw new BadRequestException("Tiered tariff requires at least one tier");
			}
			return request.getTiers();
		}
		if (request.getFlatRatePerUnit() == null) {
			throw new BadRequestException("Flat tariff requires flatRatePerUnit");
		}
		TariffCreateRequest.TariffTierRequest flatTier = new TariffCreateRequest.TariffTierRequest();
		flatTier.setTierFrom(BigDecimal.ZERO);
		flatTier.setTierTo(FLAT_TIER_MAX);
		flatTier.setRatePerUnit(request.getFlatRatePerUnit());
		return List.of(flatTier);
	}

	private TariffVersion getTariff(Long id) {
		return tariffVersionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id));
	}

	private TariffResponse toResponse(TariffVersion tariff) {
		List<TariffTier> tiers = tariffTierRepository.findByTariffVersionIdOrderByTierFromAsc(tariff.getId());
		return TariffResponse.builder()
				.id(tariff.getId())
				.meterType(tariff.getMeterType())
				.versionNumber(tariff.getVersionNumber())
				.effectiveFrom(tariff.getEffectiveFrom())
				.fixedServiceCharge(tariff.getFixedServiceCharge())
				.vatRate(tariff.getVatRate())
				.latePenaltyRate(tariff.getLatePenaltyRate())
				.tariffType(tariff.getTariffType())
				.tierBased(tariff.isTierBased())
				.tiers(tiers.stream().map(t -> TariffResponse.TariffTierResponse.builder()
						.id(t.getId())
						.tierFrom(t.getTierFrom())
						.tierTo(t.getTierTo())
						.ratePerUnit(t.getRatePerUnit())
						.build()).collect(Collectors.toList()))
				.createdAt(tariff.getCreatedAt())
				.build();
	}
}
