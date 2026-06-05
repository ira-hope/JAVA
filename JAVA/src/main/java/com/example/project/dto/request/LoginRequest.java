package com.example.project.dto.request;

/**
 * Request body for user login with email and password.
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	private String password;
}
