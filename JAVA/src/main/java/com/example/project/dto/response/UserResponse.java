package com.example.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private boolean enabled;
	private boolean emailVerified;
	private Set<String> roles;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
