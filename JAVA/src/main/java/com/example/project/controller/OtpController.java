package com.example.project.controller;

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
@Tag(name = "OTP")
public class OtpController {

	private final OTPService otpService;

	@PostMapping("/send")
	@Operation(summary = "Generate and send OTP")
	public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody OtpRequest request) {
		otpService.generateAndSend(request);
		return ResponseUtil.success(null, "OTP sent");
	}

	@PostMapping("/verify")
	@Operation(summary = "Verify OTP")
	public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyOtpRequest request) {
		otpService.verify(request);
		return ResponseUtil.success(null, "OTP verified");
	}

	@PostMapping("/resend")
	@Operation(summary = "Resend OTP")
	public ResponseEntity<ApiResponse<Void>> resend(@Valid @RequestBody OtpRequest request) {
		otpService.resend(request);
		return ResponseUtil.success(null, "OTP resent");
	}
}
