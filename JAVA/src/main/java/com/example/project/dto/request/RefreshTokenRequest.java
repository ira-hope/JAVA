package com.example.project.dto.request;

/**
 * Request body for refreshing JWT access and refresh tokens.
 */

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

	@NotBlank
	private String refreshToken;
}
