package com.example.project.dto.request;

/**
 * Request body for updating user details and roles.
 */

import com.example.project.entity.enums.UserStatus;
import com.example.project.validation.ValidName;
import com.example.project.validation.ValidRoles;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserUpdateRequest {

	@ValidName
	private String fullName;

	private String phone;

	private UserStatus status;

	private Boolean adminApproved;

	@ValidRoles
	private Set<String> roles;
}
