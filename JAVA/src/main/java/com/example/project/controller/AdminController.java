package com.example.project.controller;

/**
 * Admin REST endpoints for audit logs.
 */

import com.example.project.dto.response.AuditLogResponse;
import com.example.project.response.ApiResponse;
import com.example.project.response.PagedResponse;
import com.example.project.service.AdminService;
import com.example.project.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "WASAC admin audit logs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

	private final AdminService adminService;

	@GetMapping("/audit-logs")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get audit logs")
	public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return ResponseUtil.success(adminService.getAuditLogs(pageable), "Audit logs retrieved");
	}
}
