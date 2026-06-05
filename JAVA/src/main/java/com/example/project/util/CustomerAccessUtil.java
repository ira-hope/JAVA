package com.example.project.util;

/**
 * Ensures customers can only access their own billing data.
 */

import com.example.project.exception.ForbiddenException;
import com.example.project.security.UserPrincipal;

public final class CustomerAccessUtil {

	private CustomerAccessUtil() {
	}

	public static void ensureCustomerOwnsResource(UserPrincipal principal, Long customerId) {
		if (principal == null || customerId == null) {
			return;
		}
		if (principal.getRoleNames().contains("ROLE_CUSTOMER")
				&& (principal.getCustomerId() == null || !principal.getCustomerId().equals(customerId))) {
			throw new ForbiddenException("You can only access your own customer records");
		}
	}

	public static boolean isCustomer(UserPrincipal principal) {
		return principal != null && principal.getRoleNames().contains("ROLE_CUSTOMER");
	}
}
