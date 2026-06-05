package com.example.project.mapper;

/**
 * Maps User entities to API responses and request DTOs.
 */

import com.example.project.dto.request.UserCreateRequest;
import com.example.project.dto.request.UserUpdateRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.User;
import com.example.project.entity.enums.UserStatus;
import com.example.project.util.EmailUtil;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		Set<String> roles = user.getRoles().stream()
				.map(role -> role.getName().name())
				.collect(Collectors.toSet());
		return UserResponse.builder()
				.id(user.getId())
				.customerId(user.getCustomerId())
				.fullName(user.getFullName())
				.email(user.getEmail())
				.phone(user.getPhone())
				.status(user.getStatus())
				.emailVerified(user.isEmailVerified())
				.requestedRole(user.getRequestedRole())
				.adminApproved(user.isAdminApproved())
				.roles(roles)
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.build();
	}

	public User toEntity(UserCreateRequest request) {
		return User.builder()
				.fullName(request.getFullName().trim())
				.email(EmailUtil.normalize(request.getEmail()))
				.phone(request.getPhone().trim())
				.status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
				.emailVerified(true)
				.adminApproved(true)
				.build();
	}

	public void updateEntity(User user, UserUpdateRequest request) {
		if (request.getFullName() != null) {
			user.setFullName(request.getFullName().trim());
		}
		if (request.getPhone() != null) {
			user.setPhone(request.getPhone().trim());
		}
		if (request.getStatus() != null) {
			user.setStatus(request.getStatus());
		}
		if (request.getAdminApproved() != null) {
			user.setAdminApproved(request.getAdminApproved());
		}
	}
}
