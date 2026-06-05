package com.example.project.config;

/**
 * Configures the email sender used for OTP and billing notifications.
 */

import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {
	// Spring Boot auto-configures JavaMailSender from application.properties
}
