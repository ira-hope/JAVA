package com.example.project.service.impl;

/**
 * Implements admin features such as paginated audit log retrieval.
 */

import com.example.project.dto.response.AuditLogResponse;
import com.example.project.entity.AuditLog;
import com.example.project.repository.AuditLogRepository;
import com.example.project.response.PagedResponse;
import com.example.project.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final AuditLogRepository auditLogRepository;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
		Page<AuditLog> page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
		return PagedResponse.<AuditLogResponse>builder()
				.content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.build();
	}

	private AuditLogResponse toResponse(AuditLog log) {
		return AuditLogResponse.builder()
				.id(log.getId())
				.action(log.getAction())
				.entityType(log.getEntityType())
				.entityId(log.getEntityId())
				.performedBy(log.getPerformedBy())
				.details(log.getDetails())
				.createdAt(log.getCreatedAt())
				.build();
	}
}
