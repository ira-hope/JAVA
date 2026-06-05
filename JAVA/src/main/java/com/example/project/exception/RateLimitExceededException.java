package com.example.project.exception;

/**
 * Thrown when too many requests are made in a short time.
 */

public class RateLimitExceededException extends RuntimeException {

	public RateLimitExceededException(String message) {
		super(message);
	}
}
