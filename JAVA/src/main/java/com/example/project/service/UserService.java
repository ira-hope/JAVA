package com.example.project.service;

/**
 * Defines user management and profile operations.
 */

import com.example.project.dto.request.UserRoleUpdateRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.enums.UserStatus;
import com.example.project.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

	PagedResponse<UserResponse> findAll(
			Pageable pageable,
			String search,
			String email,
			String fullName,
			UserStatus status,
			String role);

	UserResponse findById(Long id);

	UserResponse getCurrentUser(String email);

	UserResponse updateRoles(Long id, UserRoleUpdateRequest request, String performedBy);

	void delete(Long id, String performedBy);
}
