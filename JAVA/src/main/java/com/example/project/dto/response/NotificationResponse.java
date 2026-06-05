package com.example.project.dto.response;

/**
 * API response shape for a customer notification.
 */

import com.example.project.entity.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

	private Long id;
	private Long customerId;
	private String message;
	private NotificationStatus status;
	private String recipientEmail;
	private boolean read;
	private LocalDateTime createdAt;
}
