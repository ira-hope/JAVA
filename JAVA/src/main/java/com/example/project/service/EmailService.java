package com.example.project.service;

/**
 * Defines email sending operations for OTP and notifications.
 */

public interface EmailService {

	void sendOtpEmail(String to, String otp);

	void sendPlainEmail(String to, String subject, String body);

	void sendAccountActivationEmail(String to, String fullName, String otp);
}
