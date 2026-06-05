package com.example.project.controller;

/**
 * REST endpoints for operators to capture meter readings.
 */

import com.example.project.dto.request.MeterReadingCreateRequest;
import com.example.project.dto.response.MeterReadingResponse;
import com.example.project.response.ApiResponse;
import com.example.project.response.PagedResponse;
import com.example.project.service.MeterReadingService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
@Tag(name = "Meter Readings", description = "WASAC monthly meter reading capture (operator)")
@SecurityRequirement(name = "bearerAuth")
public class MeterReadingController {

	private final MeterReadingService meterReadingService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "List all meter readings")
	public ResponseEntity<ApiResponse<PagedResponse<MeterReadingResponse>>> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return ResponseUtil.success(meterReadingService.findAll(pageable), "Readings retrieved");
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "Get reading by ID")
	public ResponseEntity<ApiResponse<MeterReadingResponse>> getById(@PathVariable Long id) {
		return ResponseUtil.success(meterReadingService.findById(id), "Reading retrieved");
	}

	@GetMapping("/meter/{meterId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "List readings for a meter")
	public ResponseEntity<ApiResponse<List<MeterReadingResponse>>> byMeter(@PathVariable Long meterId) {
		return ResponseUtil.success(meterReadingService.findByMeterId(meterId), "Meter readings retrieved");
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "Capture meter reading",
			description = "Records monthly reading. Current must exceed previous; one reading per meter per month.")
	public ResponseEntity<ApiResponse<MeterReadingResponse>> capture(@Valid @RequestBody MeterReadingCreateRequest request) {
		return ResponseUtil.created(meterReadingService.capture(request), "Reading captured");
	}
}
