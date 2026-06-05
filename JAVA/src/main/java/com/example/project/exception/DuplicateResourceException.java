package com.example.project.exception;

/**
 * Thrown when creating a record that already exists.
 */

public class DuplicateResourceException extends RuntimeException {

	public DuplicateResourceException(String message) {
		super(message);
	}
}
