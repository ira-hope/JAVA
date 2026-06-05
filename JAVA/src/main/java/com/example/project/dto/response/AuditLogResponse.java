package com.example.project.dto.response;

/**
 * API response shape for a single audit log entry.
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

	private Long id;
	private String action;
	private String entityType;
	private Long entityId;
	private String performedBy;
	private String details;
	private LocalDateTime createdAt;
}
