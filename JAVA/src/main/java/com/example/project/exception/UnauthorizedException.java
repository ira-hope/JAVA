package com.example.project.exception;

/**
 * Thrown when authentication fails or credentials are invalid.
 */

public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException(String message) {
		super(message);
	}
}
