package com.example.project.util;

import com.example.project.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ResponseUtil {

	private ResponseUtil() {
	}

	public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
		return ResponseEntity.ok(ApiResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.build());
	}

	public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.build());
	}

	public static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(ApiResponse.<Void>builder()
				.success(false)
				.message(message)
				.build());
	}
}
