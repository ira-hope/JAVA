package com.example.project.dto.request;

/**
 * Request body for registering a new utility customer.
 */

import com.example.project.entity.enums.CustomerStatus;
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
public class CustomerCreateRequest {

	@NotBlank(message = "Full name is required")
	@ValidName
	private String fullName;

	@NotBlank(message = "National ID is required")
	@ValidNationalId
	private String nationalId;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
	private String phone;

	@NotBlank(message = "Address is required")
	@Size(max = 500)
	private String address;

	private CustomerStatus status;
}
