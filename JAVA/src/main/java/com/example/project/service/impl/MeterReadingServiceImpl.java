package com.example.project.service.impl;

/**
 * Implements meter reading validation, monthly capture rules, and optional auto-billing.
 */

import com.example.project.dto.request.MeterReadingCreateRequest;
import com.example.project.dto.response.MeterReadingResponse;
import com.example.project.entity.Meter;
import com.example.project.entity.MeterReading;
import com.example.project.entity.enums.MeterStatus;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.DuplicateResourceException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.MeterReadingRepository;
import com.example.project.repository.MeterRepository;
import com.example.project.response.PagedResponse;
import com.example.project.service.BillService;
import com.example.project.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

	private static final Logger log = LoggerFactory.getLogger(MeterReadingServiceImpl.class);

	private final MeterReadingRepository meterReadingRepository;
	private final MeterRepository meterRepository;
	private final BillService billService;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<MeterReadingResponse> findAll(Pageable pageable) {
		Page<MeterReading> page = meterReadingRepository.findAll(pageable);
		return toPagedResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public MeterReadingResponse findById(Long id) {
		return toResponse(getReading(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<MeterReadingResponse> findByMeterId(Long meterId) {
		ensureMeterExists(meterId);
		return meterReadingRepository.findByMeterIdOrderByReadingDateDesc(meterId).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public MeterReadingResponse capture(MeterReadingCreateRequest request) {
		Meter meter = meterRepository.findById(request.getMeterId())
				.orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + request.getMeterId()));

		if (meter.getStatus() != MeterStatus.ACTIVE) {
			throw new BadRequestException("Readings can only be captured for active meters");
		}

		int year = request.getReadingDate().getYear();
		int month = request.getReadingDate().getMonthValue();

		if (meterReadingRepository.existsByMeterIdAndReadingYearAndReadingMonth(meter.getId(), year, month)) {
			throw new DuplicateResourceException("A reading already exists for this meter in " + year + "-" + month);
		}

		BigDecimal previousReading = meterReadingRepository
				.findTopByMeterIdOrderByReadingDateDesc(meter.getId())
				.map(MeterReading::getCurrentReading)
				.orElse(BigDecimal.ZERO);

		BigDecimal currentReading = request.getCurrentReading();
		if (currentReading.compareTo(previousReading) <= 0) {
			throw new BadRequestException("Current reading must be greater than previous reading (" + previousReading + ")");
		}

		MeterReading reading = MeterReading.builder()
				.meter(meter)
				.previousReading(previousReading)
				.currentReading(currentReading)
				.readingDate(request.getReadingDate())
				.readingYear(year)
				.readingMonth(month)
				.build();

		reading = meterReadingRepository.save(reading);

		try {
			billService.generateFromReading(reading.getId());
		} catch (Exception ex) {
			log.warn("Auto bill generation skipped for reading {}: {}", reading.getId(), ex.getMessage());
		}

		return toResponse(reading);
	}

	private void ensureMeterExists(Long meterId) {
		if (!meterRepository.existsById(meterId)) {
			throw new ResourceNotFoundException("Meter not found with id: " + meterId);
		}
	}

	private MeterReading getReading(Long id) {
		return meterReadingRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Meter reading not found with id: " + id));
	}

	private MeterReadingResponse toResponse(MeterReading reading) {
		BigDecimal usage = reading.getCurrentReading().subtract(reading.getPreviousReading());
		return MeterReadingResponse.builder()
				.id(reading.getId())
				.meterId(reading.getMeter().getId())
				.meterNumber(reading.getMeter().getMeterNumber())
				.previousReading(reading.getPreviousReading())
				.currentReading(reading.getCurrentReading())
				.usageUnits(usage)
				.readingDate(reading.getReadingDate())
				.readingYear(reading.getReadingYear())
				.readingMonth(reading.getReadingMonth())
				.createdAt(reading.getCreatedAt())
				.build();
	}

	private PagedResponse<MeterReadingResponse> toPagedResponse(Page<MeterReading> page) {
		return PagedResponse.<MeterReadingResponse>builder()
				.content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.build();
	}
}
