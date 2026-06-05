package com.example.project.util;

/**
 * Generates random numeric OTP codes for email verification.
 */

import java.security.SecureRandom;

public final class OTPGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();

	private OTPGenerator() {
	}

	public static String generate(int length) {
		StringBuilder otp = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			otp.append(RANDOM.nextInt(10));
		}
		return otp.toString();
	}
}
