package com.example.project.controller;

/**
 * REST endpoints for viewing and marking customer notifications.
 */

import com.example.project.dto.response.NotificationResponse;
import com.example.project.response.ApiResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.NotificationService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "WASAC customer notification inbox")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "List all notifications")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> listAll() {
		return ResponseUtil.success(notificationService.findAll(), "Notifications retrieved");
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "List notifications for a customer")
	public ResponseEntity<ApiResponse<List<NotificationResponse>>> byCustomer(
			@PathVariable Long customerId,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(notificationService.findByCustomerId(customerId, principal), "Notifications retrieved");
	}

	@PutMapping("/{id}/read")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Mark notification as read")
	public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(notificationService.markAsRead(id, principal), "Notification marked as read");
	}
}
