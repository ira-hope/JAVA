package com.example.project.dto.request;

/**
 * Request body for admin creation of a new system user.
 */

import com.example.project.entity.enums.UserStatus;
import com.example.project.validation.StrongPassword;
import com.example.project.validation.ValidName;
import com.example.project.validation.ValidRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserCreateRequest {

	@NotBlank(message = "Full name is required")
	@ValidName
	private String fullName;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	@Size(max = 255)
	private String email;

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
	private String phone;

	@NotBlank(message = "Password is required")
	@StrongPassword
	private String password;

	private UserStatus status;

	@ValidRoles
	private Set<String> roles;
}
