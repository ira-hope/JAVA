package com.example.project.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserUpdateRequest {

	@Size(max = 100)
	private String firstName;

	@Size(max = 100)
	private String lastName;

	private Boolean enabled;

	private Set<String> roles;
}
