package com.example.project.service.impl;

import com.example.project.dto.request.UserCreateRequest;
import com.example.project.dto.request.UserUpdateRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.entity.User;
import com.example.project.exception.DuplicateResourceException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.mapper.UserMapper;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import com.example.project.response.PagedResponse;
import com.example.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<UserResponse> findAll(Pageable pageable, String search) {
		Page<User> page = StringUtils.hasText(search)
				? userRepository.findByDeletedFalseAndEmailContainingIgnoreCase(search, pageable)
				: userRepository.findByDeletedFalse(pageable);
		return toPagedResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findById(Long id) {
		return userMapper.toResponse(getActiveUser(id));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(String email) {
		User user = userRepository.findByEmailAndDeletedFalse(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return userMapper.toResponse(user);
	}

	@Override
	@Transactional
	public UserResponse create(UserCreateRequest request) {
		if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
			throw new DuplicateResourceException("Email already exists");
		}
		User user = userMapper.toEntity(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRoles(resolveRoles(request.getRoles()));
		return userMapper.toResponse(userRepository.save(user));
	}

	@Override
	@Transactional
	public UserResponse update(Long id, UserUpdateRequest request) {
		User user = getActiveUser(id);
		userMapper.updateEntity(user, request);
		if (request.getRoles() != null) {
			user.setRoles(resolveRoles(request.getRoles()));
		}
		return userMapper.toResponse(userRepository.save(user));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		User user = getActiveUser(id);
		user.setDeleted(true);
		user.setEnabled(false);
		userRepository.save(user);
	}

	private User getActiveUser(Long id) {
		return userRepository.findByIdAndDeletedFalse(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private Set<Role> resolveRoles(Set<String> roleNames) {
		if (roleNames == null || roleNames.isEmpty()) {
			return Set.of(roleRepository.findByName(RoleName.ROLE_USER)
					.orElseThrow(() -> new IllegalStateException("ROLE_USER not found")));
		}
		Set<Role> roles = new HashSet<>();
		for (String roleName : roleNames) {
			RoleName name = RoleName.valueOf(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
			roles.add(roleRepository.findByName(name)
					.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)));
		}
		return roles;
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
