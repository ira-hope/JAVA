package com.example.project.service;

import com.example.project.dto.request.OtpRequest;
import com.example.project.dto.request.VerifyOtpRequest;

public interface OTPService {

	void generateAndSend(OtpRequest request);

	void verify(VerifyOtpRequest request);

	void resend(OtpRequest request);
}
