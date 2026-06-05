package com.example.project.dto.request;

/**
 * Request body for sending or resending an OTP to an email address.
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpRequest {

	@NotBlank
	@Email
	private String email;
}
