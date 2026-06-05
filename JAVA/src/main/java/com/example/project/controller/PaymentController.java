package com.example.project.controller;

/**
 * REST endpoints to record and look up bill payments.
 */

import com.example.project.dto.request.PaymentCreateRequest;
import com.example.project.dto.response.PaymentResponse;
import com.example.project.response.ApiResponse;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.PaymentService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "WASAC bill payment recording (partial or full)")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
	@Operation(summary = "Record customer payment",
			description = "Finance records payment using bill reference, amount paid, payment method, and payment date")
	public ResponseEntity<ApiResponse<PaymentResponse>> record(
			@Valid @RequestBody PaymentCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.created(paymentService.recordPayment(request, principal), "Payment recorded");
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
	@Operation(summary = "List all payments")
	public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return ResponseUtil.success(paymentService.findAll(pageable), "Payments retrieved");
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List payments by customer")
	public ResponseEntity<ApiResponse<List<PaymentResponse>>> byCustomer(
			@PathVariable Long customerId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(paymentService.findByCustomerId(customerId, principal), "Customer payments retrieved");
	}

	@GetMapping("/bill/{billId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List payments for a bill")
	public ResponseEntity<ApiResponse<List<PaymentResponse>>> byBill(
			@PathVariable Long billId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(paymentService.findByBillId(billId, principal), "Bill payments retrieved");
	}
}
