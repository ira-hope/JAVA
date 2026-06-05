package com.example.project.dto.request;

/**
 * Request body for updating an existing customer record.
 */

import com.example.project.entity.enums.CustomerStatus;
import com.example.project.validation.ValidName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerUpdateRequest {

	@ValidName
	private String fullName;

	@Email(message = "Invalid email format")
	private String email;

	@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
	private String phone;

	@Size(max = 500)
	private String address;

	private CustomerStatus status;
}
