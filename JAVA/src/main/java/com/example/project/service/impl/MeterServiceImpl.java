package com.example.project.service.impl;

/**
 * Implements meter installation and customer linkage logic.
 */

import com.example.project.dto.request.MeterCreateRequest;
import com.example.project.dto.response.MeterResponse;
import com.example.project.entity.Customer;
import com.example.project.entity.Meter;
import com.example.project.entity.enums.MeterStatus;
import com.example.project.exception.DuplicateResourceException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.CustomerRepository;
import com.example.project.repository.MeterRepository;
import com.example.project.response.PagedResponse;
import com.example.project.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {

	private final MeterRepository meterRepository;
	private final CustomerRepository customerRepository;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<MeterResponse> findAll(Pageable pageable) {
		Page<Meter> page = meterRepository.findAll(pageable);
		return toPagedResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public MeterResponse findById(Long id) {
		return toResponse(getMeter(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<MeterResponse> findByCustomerId(Long customerId) {
		ensureCustomerExists(customerId);
		return meterRepository.findByCustomerId(customerId).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public MeterResponse create(MeterCreateRequest request) {
		if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
			throw new DuplicateResourceException("Meter number already exists");
		}
		Customer customer = customerRepository.findById(request.getCustomerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
		Meter meter = Meter.builder()
				.meterNumber(request.getMeterNumber().trim())
				.meterType(request.getMeterType())
				.installationDate(request.getInstallationDate())
				.status(request.getStatus() != null ? request.getStatus() : MeterStatus.ACTIVE)
				.customer(customer)
				.build();
		return toResponse(meterRepository.save(meter));
	}

	@Override
	@Transactional
	public MeterResponse update(Long id, MeterCreateRequest request) {
		Meter meter = getMeter(id);
		meterRepository.findByMeterNumber(request.getMeterNumber())
				.filter(existing -> !existing.getId().equals(id))
				.ifPresent(existing -> {
					throw new DuplicateResourceException("Meter number already exists");
				});
		Customer customer = customerRepository.findById(request.getCustomerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
		meter.setMeterNumber(request.getMeterNumber().trim());
		meter.setMeterType(request.getMeterType());
		meter.setInstallationDate(request.getInstallationDate());
		if (request.getStatus() != null) {
			meter.setStatus(request.getStatus());
		}
		meter.setCustomer(customer);
		return toResponse(meterRepository.save(meter));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		meterRepository.delete(getMeter(id));
	}

	private void ensureCustomerExists(Long customerId) {
		if (!customerRepository.existsById(customerId)) {
			throw new ResourceNotFoundException("Customer not found with id: " + customerId);
		}
	}

	private Meter getMeter(Long id) {
		return meterRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
	}

	private MeterResponse toResponse(Meter meter) {
		return MeterResponse.builder()
				.id(meter.getId())
				.meterNumber(meter.getMeterNumber())
				.meterType(meter.getMeterType())
				.installationDate(meter.getInstallationDate())
				.status(meter.getStatus())
				.customerId(meter.getCustomer().getId())
				.customerName(meter.getCustomer().getFullName())
				.createdAt(meter.getCreatedAt())
				.updatedAt(meter.getUpdatedAt())
				.build();
	}

	private PagedResponse<MeterResponse> toPagedResponse(Page<Meter> page) {
		return PagedResponse.<MeterResponse>builder()
				.content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.build();
	}
}
