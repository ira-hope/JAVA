package com.example.project.util;

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
