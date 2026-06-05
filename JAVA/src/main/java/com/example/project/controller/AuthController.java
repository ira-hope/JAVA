package com.example.project.controller;

/**
 * Handles user registration, login, and JWT token refresh.
 */

import com.example.project.dto.request.ActivateAccountRequest;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RefreshTokenRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.response.ApiResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.AuthService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "WASAC user registration, login, and JWT token management")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@Operation(summary = "Register a user",
			description = """
					Customer self-registration (no role field): requires fullName, nationalId, email, phone, \
					address, and optional status. OTP is sent automatically and login is blocked until \
					POST /api/otp/verify succeeds. \
					Authenticated admins may include role to create staff (ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE) — no OTP.
					""")
	public ResponseEntity<ApiResponse<Void>> register(
			@Valid @RequestBody RegisterRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		String message = authService.register(request, principal);
		return ResponseUtil.created(null, message);
	}

	@PostMapping("/activate-account")
	@Operation(summary = "Activate admin-created customer account",
			description = "Customer verifies OTP from email and sets their own password. No admin password is required.")
	public ResponseEntity<ApiResponse<Void>> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
		authService.activateAccount(request);
		return ResponseUtil.success(null, "Account activated. You can now log in.");
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Returns JWT access and refresh tokens")
	@io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = """
			{
			  "email": "admin@wasac.com",
			  "password": "admin123"
			}
			""")))
	public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return ResponseUtil.success(authService.login(request), "Login successful");
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh JWT tokens")
	public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ResponseUtil.success(authService.refresh(request), "Token refreshed");
	}
}
