package com.example.project.exception;

/**
 * Thrown when a requested record cannot be found.
 */

public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
