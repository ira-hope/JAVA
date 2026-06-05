package com.example.project.service;

/**
 * Defines meter reading capture and lookup operations.
 */

import com.example.project.dto.request.MeterReadingCreateRequest;
import com.example.project.dto.response.MeterReadingResponse;
import com.example.project.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MeterReadingService {

	PagedResponse<MeterReadingResponse> findAll(Pageable pageable);

	MeterReadingResponse findById(Long id);

	List<MeterReadingResponse> findByMeterId(Long meterId);

	MeterReadingResponse capture(MeterReadingCreateRequest request);
}
