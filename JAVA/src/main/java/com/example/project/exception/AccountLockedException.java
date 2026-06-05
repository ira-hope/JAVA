package com.example.project.exception;

/**
 * Thrown when login is blocked because the account is locked.
 */

public class AccountLockedException extends RuntimeException {

	public AccountLockedException(String message) {
		super(message);
	}
}
