package com.example.project.dto.request;

/**
 * Request body for admin role assignment on a user account.
 */

import com.example.project.validation.ValidRoles;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserRoleUpdateRequest {

	@NotEmpty(message = "At least one role is required")
	@ValidRoles
	private Set<String> roles;
}
