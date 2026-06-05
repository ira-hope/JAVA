package com.example.project.util;

/**
 * Normalizes and validates email addresses before saving or sending.
 */

import java.util.Locale;

public final class EmailUtil {

	private EmailUtil() {
	}

	public static String normalize(String email) {
		if (email == null) {
			return null;
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
