package com.example.project.service.impl;

import com.example.project.dto.request.OtpRequest;
import com.example.project.dto.request.VerifyOtpRequest;
import com.example.project.entity.OtpVerification;
import com.example.project.entity.User;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.OtpVerificationRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.EmailService;
import com.example.project.service.OTPService;
import com.example.project.util.DateUtil;
import com.example.project.util.OTPGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {

	private final OtpVerificationRepository otpRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Value("${app.otp.expiration-minutes}")
	private int expirationMinutes;

	@Value("${app.otp.length}")
	private int otpLength;

	@Override
	@Transactional
	public void generateAndSend(OtpRequest request) {
		userRepository.findByEmailAndDeletedFalse(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		saveAndSend(request.getEmail());
	}

	@Override
	@Transactional
	public void resend(OtpRequest request) {
		generateAndSend(request);
	}

	@Override
	@Transactional
	public void verify(VerifyOtpRequest request) {
		OtpVerification record = otpRepository
				.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(request.getEmail())
				.orElseThrow(() -> new BadRequestException("No active OTP found"));

		if (DateUtil.isExpired(record.getExpiresAt())) {
			throw new BadRequestException("OTP has expired");
		}
		if (!record.getOtp().equals(request.getOtp())) {
			throw new BadRequestException("Invalid OTP");
		}

		record.setVerified(true);
		otpRepository.save(record);

		User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		user.setEmailVerified(true);
		userRepository.save(user);
	}

	private void saveAndSend(String email) {
		String otp = OTPGenerator.generate(otpLength);
		OtpVerification verification = OtpVerification.builder()
				.email(email)
				.otp(otp)
				.expiresAt(DateUtil.plusMinutes(expirationMinutes))
				.verified(false)
				.build();
		otpRepository.save(verification);
		emailService.sendOtpEmail(email, otp);
	}
}
