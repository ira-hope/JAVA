package com.example.project.dto.request;

/**
 * Request body for admin approval or rejection of a role request.
 */

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleApprovalRequest {

	@NotNull(message = "User ID is required")
	private Long userId;

	@NotNull(message = "Approve flag is required")
	private Boolean approve;
}
