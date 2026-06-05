package com.example.project.validation;

/**
 * Checks that a name contains only allowed characters.
 */

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidNameValidator implements ConstraintValidator<ValidName, String> {

	private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} '\\-]{1,99}$");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		return NAME_PATTERN.matcher(value.trim()).matches();
	}
}
