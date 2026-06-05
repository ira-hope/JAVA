package com.example.project.validation;

/**
 * Validation annotation for national ID numbers (digits only).
 */

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidNationalIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNationalId {

	String message() default "National ID must be 5-20 digits";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
