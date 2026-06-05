package com.example.project.validation;

/**
 * Checks that submitted role names are valid WASAC roles.
 */

import com.example.project.entity.Role.RoleName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class ValidRolesValidator implements ConstraintValidator<ValidRoles, Set<String>> {

	@Override
	public boolean isValid(Set<String> roles, ConstraintValidatorContext context) {
		if (roles == null || roles.isEmpty()) {
			return true;
		}
		for (String role : roles) {
			if (role == null || role.isBlank()) {
				return false;
			}
			String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
			try {
				RoleName.valueOf(normalized.toUpperCase());
			} catch (IllegalArgumentException ex) {
				return false;
			}
		}
		return true;
	}
}
