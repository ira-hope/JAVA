package com.example.project.dto.request;

/**
 * Request body for admin-invited customers to verify OTP and set their password.
 */

import com.example.project.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateAccountRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 4, max = 10)
	@Pattern(regexp = "\\d+")
	private String otp;

	@NotBlank
	@StrongPassword
	private String password;
}
