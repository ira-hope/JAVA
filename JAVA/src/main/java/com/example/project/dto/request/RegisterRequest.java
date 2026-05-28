package com.example.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 8, max = 100)
	private String password;

	@NotBlank
	@Size(max = 100)
	private String firstName;

	@NotBlank
	@Size(max = 100)
	private String lastName;

	/**
	 * Optional roles for account creation, e.g. ["ROLE_USER"], ["ROLE_ADMIN"], ["ROLE_MANAGER"].
	 * If omitted, defaults to ROLE_USER.
	 */
	private Set<String> roles;
}
