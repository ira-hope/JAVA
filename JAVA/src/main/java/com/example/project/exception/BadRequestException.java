package com.example.project.exception;

/**
 * Thrown when the client sends invalid or unacceptable input.
 */

public class BadRequestException extends RuntimeException {

	public BadRequestException(String message) {
		super(message);
	}
}
