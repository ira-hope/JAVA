package com.example.project.controller;

/**
 * REST endpoints for admin user management and authenticated account lookup.
 */

import com.example.project.dto.request.UserRoleUpdateRequest;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.enums.UserStatus;
import com.example.project.response.ApiResponse;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.UserService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin user administration and self account lookup")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "List all users", description = "Admin views every account in the system")
	public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAll(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
			@Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
			@Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String direction,
			@Parameter(description = "Search across email, full name, phone") @RequestParam(required = false) String search,
			@Parameter(description = "Filter by email") @RequestParam(required = false) String email,
			@Parameter(description = "Filter by full name") @RequestParam(required = false) String fullName,
			@Parameter(description = "Filter by status: ACTIVE or INACTIVE") @RequestParam(required = false) UserStatus status,
			@Parameter(description = "Filter by role (e.g. ADMIN, OPERATOR, FINANCE, CUSTOMER)") @RequestParam(required = false) String role) {
		Sort sort = direction.equalsIgnoreCase("asc")
				? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		return ResponseUtil.success(
				userService.findAll(pageable, search, email, fullName, status, role),
				"Users retrieved");
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get my account", description = "Returns the authenticated user's account details")
	public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(userService.getCurrentUser(principal.getEmail()), "Current user retrieved");
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get user by ID")
	public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
		return ResponseUtil.success(userService.findById(id), "User retrieved");
	}

	@PutMapping("/{id}/roles")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Upgrade or revoke user role",
			description = "Admin assigns exactly one role to the user account")
	public ResponseEntity<ApiResponse<UserResponse>> updateRoles(
			@PathVariable Long id,
			@Valid @RequestBody UserRoleUpdateRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		return ResponseUtil.success(
				userService.updateRoles(id, request, principal.getEmail()),
				"User roles updated");
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete user", description = "Permanently deletes a user and linked customer data when applicable")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal principal) {
		userService.delete(id, principal.getEmail());
		return ResponseUtil.success(null, "User permanently deleted");
	}
}
