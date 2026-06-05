package com.example.project.util;

/**
 * Small date and time helper methods used across the app.
 */

import java.time.LocalDateTime;

public final class DateUtil {

	private DateUtil() {
	}

	public static LocalDateTime now() {
		return LocalDateTime.now();
	}

	public static LocalDateTime plusMinutes(long minutes) {
		return LocalDateTime.now().plusMinutes(minutes);
	}

	public static boolean isExpired(LocalDateTime expiresAt) {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}
