package com.example.project.controller;

/**
 * REST endpoints for installing and managing customer meters.
 */

import com.example.project.dto.request.MeterCreateRequest;
import com.example.project.dto.response.MeterResponse;
import com.example.project.response.ApiResponse;
import com.example.project.response.PagedResponse;
import com.example.project.service.MeterService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meters", description = "WASAC water/electricity meter management")
@SecurityRequirement(name = "bearerAuth")
public class MeterController {

	private final MeterService meterService;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "List meters")
	public ResponseEntity<ApiResponse<PagedResponse<MeterResponse>>> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return ResponseUtil.success(meterService.findAll(pageable), "Meters retrieved");
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "Get meter by ID")
	public ResponseEntity<ApiResponse<MeterResponse>> getById(@PathVariable Long id) {
		return ResponseUtil.success(meterService.findById(id), "Meter retrieved");
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "List meters for a customer")
	public ResponseEntity<ApiResponse<List<MeterResponse>>> byCustomer(@PathVariable Long customerId) {
		return ResponseUtil.success(meterService.findByCustomerId(customerId), "Customer meters retrieved");
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Install meter", description = "Admin assigns a new meter to a WASAC customer")
	public ResponseEntity<ApiResponse<MeterResponse>> create(@Valid @RequestBody MeterCreateRequest request) {
		return ResponseUtil.created(meterService.create(request), "Meter created");
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "Update meter")
	public ResponseEntity<ApiResponse<MeterResponse>> update(
			@PathVariable Long id,
			@Valid @RequestBody MeterCreateRequest request) {
		return ResponseUtil.success(meterService.update(id, request), "Meter updated");
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete meter")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		meterService.delete(id);
		return ResponseUtil.success(null, "Meter deleted");
	}
}
