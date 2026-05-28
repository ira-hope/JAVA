package com.example.project.service.impl;

import com.example.project.exception.BadRequestException;
import com.example.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

	private final JavaMailSender mailSender;

	@Value("${app.mail.from}")
	private String from;

	@Value("${spring.mail.username:}")
	private String smtpUsername;

	@Value("${spring.mail.password:}")
	private String smtpPassword;

	@Override
	public void sendOtpEmail(String to, String otp) {
		sendPlainEmail(to, "Your verification code", "Your OTP is: " + otp + ". It expires soon.");
	}

	@Override
	public void sendPlainEmail(String to, String subject, String body) {
		if (!StringUtils.hasText(smtpUsername) || !StringUtils.hasText(smtpPassword)) {
			throw new BadRequestException("Email sending is not configured. Set MAIL_USERNAME and MAIL_PASSWORD.");
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
			log.info("Email sent to {}", to);
		} catch (Exception ex) {
			log.warn("Failed to send email to {}: {}", to, ex.getMessage());
			throw new BadRequestException("Failed to send email: " + ex.getMessage());
		}
	}
}
