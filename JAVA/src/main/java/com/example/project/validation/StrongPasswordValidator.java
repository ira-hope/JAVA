package com.example.project.validation;

/**
 * Checks that a password meets WASAC strength rules.
 */

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

	private static final Pattern STRONG_PASSWORD = Pattern.compile(
			"^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_\\-+=\\[\\]{}:;\"',.<>/\\\\|`~]).{8,100}$");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return value != null && STRONG_PASSWORD.matcher(value).matches();
	}
}
