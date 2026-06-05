package com.example.project.service;

/**
 * Defines tariff version create, update, and lookup operations.
 */

import com.example.project.dto.request.TariffCreateRequest;
import com.example.project.dto.request.TariffUpdateRequest;
import com.example.project.dto.response.TariffResponse;
import com.example.project.entity.enums.MeterType;

import java.util.List;

public interface TariffService {

	List<TariffResponse> findAll();

	List<TariffResponse> findByMeterType(MeterType meterType);

	TariffResponse findActiveByMeterType(MeterType meterType);

	TariffResponse findById(Long id);

	TariffResponse create(TariffCreateRequest request);

	TariffResponse update(Long id, TariffUpdateRequest request);
}
