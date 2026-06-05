package com.example.project.exception;

/**
 * Thrown when the user is authenticated but not allowed to act.
 */

public class ForbiddenException extends RuntimeException {

	public ForbiddenException(String message) {
		super(message);
	}
}
