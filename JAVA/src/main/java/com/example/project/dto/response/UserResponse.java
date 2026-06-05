package com.example.project.dto.response;

/**
 * API response shape for user account details.
 */

import com.example.project.entity.enums.RequestedRole;
import com.example.project.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private Long id;
	private Long customerId;
	private String fullName;
	private String email;
	private String phone;
	private UserStatus status;
	private boolean emailVerified;
	private RequestedRole requestedRole;
	private boolean adminApproved;
	private Set<String> roles;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
