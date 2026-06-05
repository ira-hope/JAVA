package com.example.project.dto.request;

/**
 * Request body for user registration. Public users become customers; admins may pass a staff role.
 */

import com.example.project.entity.enums.CustomerStatus;
import com.example.project.validation.StrongPassword;
import com.example.project.validation.ValidName;
import com.example.project.validation.ValidNationalId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

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

	/** Required for customer self-registration; ignored for staff accounts created by admin. */
	@ValidNationalId
	private String nationalId;

	/** Required for customer self-registration; ignored for staff accounts created by admin. */
	@Size(max = 500, message = "Address must not exceed 500 characters")
	private String address;

	/** Customer account status; defaults to ACTIVE when omitted. */
	private CustomerStatus status;

	/** Optional staff role — only allowed when an admin is authenticated (ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE). */
	private String role;
}
