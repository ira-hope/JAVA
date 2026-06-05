package com.example.project.controller;

/**
 * REST endpoints for customer self-service profile management.
 */

import com.example.project.dto.request.CustomerCreateRequest;
import com.example.project.dto.request.CustomerUpdateRequest;
import com.example.project.dto.response.CustomerResponse;
import com.example.project.response.ApiResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.CustomerService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Admin customer onboarding and customer self-service profile")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

	private final CustomerService customerService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Create customer (admin)",
			description = "Admin creates customer profile and user account without a password. "
					+ "An activation email with OTP is sent so the customer can set their password via "
					+ "POST /api/auth/activate-account.")
	public ResponseEntity<ApiResponse<CustomerResponse>> create(
			@Valid @RequestBody CustomerCreateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.created(
				customerService.createByAdmin(request, principal.getEmail()),
				"Customer created. Activation email sent.");
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('CUSTOMER')")
	@Operation(summary = "Get my profile", description = "Customer views their utility profile")
	public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile(
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(customerService.getMyProfile(principal), "Customer profile retrieved");
	}

	@PutMapping("/me")
	@PreAuthorize("hasRole('CUSTOMER')")
	@Operation(summary = "Update my profile", description = "Customer updates full name, email, phone, and address")
	public ResponseEntity<ApiResponse<CustomerResponse>> updateMyProfile(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody CustomerUpdateRequest request) {
		return ResponseUtil.success(customerService.updateMyProfile(principal, request), "Customer profile updated");
	}
}
