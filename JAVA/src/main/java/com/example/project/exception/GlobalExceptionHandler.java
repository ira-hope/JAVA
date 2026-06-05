package com.example.project.exception;

/**
 * Converts application exceptions into consistent API error responses.
 */

import com.example.project.response.ApiResponse;
import com.example.project.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
		return ResponseUtil.error(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler({BadRequestException.class, ValidationException.class})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
		return ResponseUtil.error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
		return ResponseUtil.error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
	public ResponseEntity<ApiResponse<Void>> handleUnauthorized(RuntimeException ex) {
		return ResponseUtil.error(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
	public ResponseEntity<ApiResponse<Void>> handleForbidden(RuntimeException ex) {
		return ResponseUtil.error(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(PendingApprovalException.class)
	public ResponseEntity<ApiResponse<Void>> handlePendingApproval(PendingApprovalException ex) {
		return ResponseUtil.error(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler({AccountLockedException.class, LockedException.class})
	public ResponseEntity<ApiResponse<Void>> handleAccountLocked(RuntimeException ex) {
		return ResponseUtil.error(HttpStatus.LOCKED, ex.getMessage());
	}

	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<ApiResponse<Void>> handleRateLimit(RateLimitExceededException ex) {
		return ResponseUtil.error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseUtil.error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleMalformedJson(HttpMessageNotReadableException ex) {
		return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Malformed or invalid JSON request body");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining("; "));
		return ResponseUtil.error(HttpStatus.BAD_REQUEST, message);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
		log.error("Unhandled exception", ex);
		return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
