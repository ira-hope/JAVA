package com.example.project.service;

public interface EmailService {

	void sendOtpEmail(String to, String otp);

	void sendPlainEmail(String to, String subject, String body);
}
