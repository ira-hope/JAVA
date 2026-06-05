package com.example.project.service;

/**
 * Defines OTP generation, delivery, and verification operations.
 */

import com.example.project.dto.request.ActivateAccountRequest;
import com.example.project.dto.request.OtpRequest;
import com.example.project.dto.request.VerifyOtpRequest;

public interface OTPService {

	void generateAndSend(OtpRequest request);

	void verify(VerifyOtpRequest request);

	void resend(OtpRequest request);

	void sendActivationInvite(String email, String fullName);

	void activateAccount(ActivateAccountRequest request);
}
