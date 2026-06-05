package com.example.project.service;

/**
 * Defines meter installation and management operations.
 */

import com.example.project.dto.request.MeterCreateRequest;
import com.example.project.dto.response.MeterResponse;
import com.example.project.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MeterService {

	PagedResponse<MeterResponse> findAll(Pageable pageable);

	MeterResponse findById(Long id);

	List<MeterResponse> findByCustomerId(Long customerId);

	MeterResponse create(MeterCreateRequest request);

	MeterResponse update(Long id, MeterCreateRequest request);

	void delete(Long id);
}
