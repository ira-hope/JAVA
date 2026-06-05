package com.example.project.controller;

/**
 * REST endpoints to send, verify, and resend email OTP codes.
 */

import com.example.project.dto.request.OtpRequest;
import com.example.project.dto.request.VerifyOtpRequest;
import com.example.project.response.ApiResponse;
import com.example.project.service.OTPService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@Tag(name = "OTP Verification", description = "Email OTP for WASAC account activation")
public class OtpController {

	private final OTPService otpService;

	@PostMapping("/send")
	@Operation(summary = "Send OTP", description = "Generates and emails a one-time password to a registered user")
	public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody OtpRequest request) {
		otpService.generateAndSend(request);
		return ResponseUtil.success(null, "OTP sent");
	}

	@PostMapping("/verify")
	@Operation(summary = "Verify OTP",
			description = """
					Validates the OTP sent after customer registration. \
					On success: email is verified, ROLE_CUSTOMER is assigned, and a customer profile is linked. \
					Login is only allowed after this step.
					""")
	public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyOtpRequest request) {
		otpService.verify(request);
		return ResponseUtil.success(null, "OTP verified. You can now log in.");
	}

	@PostMapping("/resend")
	@Operation(summary = "Resend OTP", description = "Generates and sends a new OTP")
	public ResponseEntity<ApiResponse<Void>> resend(@Valid @RequestBody OtpRequest request) {
		otpService.resend(request);
		return ResponseUtil.success(null, "OTP resent");
	}
}
