package com.example.project.validation;

/**
 * Shared helper methods for input validation.
 */

import com.example.project.exception.ValidationException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class ValidationUtil {

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private ValidationUtil() {
	}

	public static void requireNonBlank(String value, String fieldName) {
		if (!StringUtils.hasText(value)) {
			throw new ValidationException(fieldName + " must not be blank");
		}
	}

	public static void requireValidEmail(String email) {
		if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
			throw new ValidationException("Invalid email format");
		}
	}
}
