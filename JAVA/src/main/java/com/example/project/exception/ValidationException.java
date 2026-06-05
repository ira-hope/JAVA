package com.example.project.exception;

/**
 * Thrown when business validation rules are violated.
 */

public class ValidationException extends RuntimeException {

	public ValidationException(String message) {
		super(message);
	}
}
