package com.example.project.controller;

/**
 * REST endpoints for bill generation, approval, and customer bill lookup.
 */

import com.example.project.dto.response.BillResponse;
import com.example.project.response.ApiResponse;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.BillService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bills", description = "WASAC bill generation, approval, and inquiry")
@SecurityRequirement(name = "bearerAuth")
public class BillController {

	private final BillService billService;

	@PostMapping("/generate/{readingId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
	@Operation(summary = "Generate bill from meter reading", description = "Creates a PENDING bill from a captured reading")
	public ResponseEntity<ApiResponse<BillResponse>> generate(@PathVariable Long readingId) {
		return ResponseUtil.created(billService.generateFromReading(readingId), "Bill generated");
	}

	@PutMapping("/{billId}/approve")
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
	@Operation(summary = "Approve bill",
			description = "Finance approves a PENDING bill so payment can be recorded via POST /api/payments")
	public ResponseEntity<ApiResponse<BillResponse>> approve(
			@PathVariable Long billId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(billService.approve(billId, principal), "Bill approved");
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
	@Operation(summary = "List all bills")
	public ResponseEntity<ApiResponse<PagedResponse<BillResponse>>> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return ResponseUtil.success(billService.findAll(pageable), "Bills retrieved");
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get bill by ID", description = "Customers can only view their own bills")
	public ResponseEntity<ApiResponse<BillResponse>> getById(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(billService.findById(id, principal), "Bill retrieved");
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List bills for a customer", description = "Customers can only view their own bills")
	public ResponseEntity<ApiResponse<List<BillResponse>>> byCustomer(
			@PathVariable Long customerId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(billService.findByCustomerId(customerId, principal), "Customer bills retrieved");
	}
}
