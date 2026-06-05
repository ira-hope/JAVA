package com.example.project.exception;

/**
 * Thrown when a user tries to log in before admin approval.
 */

public class PendingApprovalException extends RuntimeException {

	public PendingApprovalException(String message) {
		super(message);
	}
}
