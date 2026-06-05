package com.example.project.service.impl;

/**
 * Implements admin user listing, role management, and account deletion.
 */

import com.example.project.dto.request.UserRoleUpdateRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.entity.User;
import com.example.project.entity.enums.UserStatus;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.mapper.UserMapper;
import com.example.project.repository.RefreshTokenRepository;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import com.example.project.response.PagedResponse;
import com.example.project.service.AuditService;
import com.example.project.service.CustomerService;
import com.example.project.service.UserService;
import com.example.project.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final AuditService auditService;
	private final CustomerService customerService;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<UserResponse> findAll(
			Pageable pageable,
			String search,
			String email,
			String fullName,
			UserStatus status,
			String role) {
		validateRoleFilter(role);
		Specification<User> spec = UserSpecification.withFilters(search, email, fullName, status, role);
		Page<User> page = userRepository.findAll(spec, pageable);
		page.getContent().forEach(user -> user.getRoles().size());
		return toPagedResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findById(Long id) {
		return userMapper.toResponse(getUser(id));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(String email) {
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return userMapper.toResponse(user);
	}

	@Override
	@Transactional
	public UserResponse updateRoles(Long id, UserRoleUpdateRequest request, String performedBy) {
		User user = getUser(id);
		Set<Role> roles = resolveRoles(request.getRoles());
		user.setRoles(roles);
		applyRequestedRole(user, roles);
		User saved = userRepository.save(user);
		auditService.log("UPDATE_ROLES", "User", saved.getId(), performedBy,
				"Roles updated to " + request.getRoles());
		return userMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public void delete(Long id, String performedBy) {
		User user = getUser(id);
		if ("admin@wasac.com".equalsIgnoreCase(user.getEmail())) {
			throw new BadRequestException("The default system administrator cannot be deleted");
		}
		String email = user.getEmail();
		if (user.getCustomerId() != null) {
			customerService.delete(user.getCustomerId());
		}
		refreshTokenRepository.deleteByUser(user);
		user.getRoles().clear();
		userRepository.delete(user);
		auditService.log("DELETE", "User", id, performedBy, "User permanently deleted: " + email);
	}

	private User getUser(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private void applyRequestedRole(User user, Set<Role> roles) {
		if (roles.isEmpty()) {
			throw new BadRequestException("At least one role is required");
		}
		if (roles.size() > 1) {
			throw new BadRequestException("A user can only have one role at a time");
		}
		RoleName primaryRole = roles.iterator().next().getName();
		user.setRequestedRole(AuthServiceImpl.mapRequestedRole(primaryRole));
		if (primaryRole != RoleName.ROLE_CUSTOMER) {
			user.setCustomerId(null);
		}
	}

	private Set<Role> resolveRoles(Set<String> roleNames) {
		Set<Role> roles = new HashSet<>();
		for (String roleName : roleNames) {
			RoleName name = RoleName.valueOf(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
			roles.add(roleRepository.findByName(name)
					.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)));
		}
		return roles;
	}

	private void validateRoleFilter(String role) {
		if (role == null || role.isBlank()) {
			return;
		}
		String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		try {
			RoleName.valueOf(roleName.toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Invalid role filter: " + role
					+ ". Use ADMIN, OPERATOR, FINANCE, or CUSTOMER.");
		}
	}

	private PagedResponse<UserResponse> toPagedResponse(Page<User> page) {
		return PagedResponse.<UserResponse>builder()
				.content(page.getContent().stream().map(userMapper::toResponse).collect(Collectors.toList()))
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.build();
	}
}
