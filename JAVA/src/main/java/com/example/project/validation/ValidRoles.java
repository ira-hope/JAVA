package com.example.project.validation;

/**
 * Validation annotation for allowed WASAC role names.
 */

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidRolesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoles {

	String message() default "Each role must be one of: ROLE_USER, ROLE_MANAGER, ROLE_ADMIN";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
