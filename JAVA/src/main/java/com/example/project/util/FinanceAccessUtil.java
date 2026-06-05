package com.example.project.util;

/**
 * Ensures only finance staff (or admins) can approve bills and record payments.
 */

import com.example.project.exception.ForbiddenException;
import com.example.project.security.UserPrincipal;

public final class FinanceAccessUtil {

	private FinanceAccessUtil() {
	}

	public static void ensureFinanceOrAdmin(UserPrincipal principal) {
		if (principal == null) {
			throw new ForbiddenException("Authentication required");
		}
		boolean allowed = principal.getRoleNames().contains("ROLE_FINANCE")
				|| principal.getRoleNames().contains("ROLE_ADMIN");
		if (!allowed) {
			throw new ForbiddenException("Only finance staff can approve bills and record payments");
		}
	}
}
