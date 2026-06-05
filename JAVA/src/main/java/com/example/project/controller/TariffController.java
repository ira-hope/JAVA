package com.example.project.controller;

/**
 * REST endpoints for admin tariff version management.
 */

import com.example.project.dto.request.TariffCreateRequest;
import com.example.project.dto.request.TariffUpdateRequest;
import com.example.project.dto.response.TariffResponse;
import com.example.project.entity.enums.MeterType;
import com.example.project.response.ApiResponse;
import com.example.project.service.TariffService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@Tag(name = "Tariffs", description = "WASAC versioned tariff management (admin)")
@SecurityRequirement(name = "bearerAuth")
public class TariffController {

	private final TariffService tariffService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "List all tariff versions")
	public ResponseEntity<ApiResponse<List<TariffResponse>>> listAll() {
		return ResponseUtil.success(tariffService.findAll(), "Tariffs retrieved");
	}

	@GetMapping("/active/{meterType}")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
	@Operation(summary = "Get active tariff by meter type", description = "WATER or ELECTRICITY")
	public ResponseEntity<ApiResponse<TariffResponse>> active(@PathVariable MeterType meterType) {
		return ResponseUtil.success(tariffService.findActiveByMeterType(meterType), "Active tariff retrieved");
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get tariff version by ID")
	public ResponseEntity<ApiResponse<TariffResponse>> getById(@PathVariable Long id) {
		return ResponseUtil.success(tariffService.findById(id), "Tariff retrieved");
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create tariff version", description = "Supports FLAT or TIERED tariff types")
	public ResponseEntity<ApiResponse<TariffResponse>> create(@Valid @RequestBody TariffCreateRequest request) {
		return ResponseUtil.created(tariffService.create(request), "Tariff version created");
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update tariff version")
	public ResponseEntity<ApiResponse<TariffResponse>> update(
			@PathVariable Long id,
			@Valid @RequestBody TariffUpdateRequest request) {
		return ResponseUtil.success(tariffService.update(id, request), "Tariff updated");
	}
}
