package com.example.project.service.impl;

/**
 * Implements writing audit log entries to the database.
 */

import com.example.project.entity.AuditLog;
import com.example.project.repository.AuditLogRepository;
import com.example.project.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

	private final AuditLogRepository auditLogRepository;

	@Override
	@Transactional
	public void log(String action, String entityType, Long entityId, String performedBy, String details) {
		auditLogRepository.save(AuditLog.builder()
				.action(action)
				.entityType(entityType)
				.entityId(entityId)
				.performedBy(performedBy)
				.details(details)
				.build());
	}
}
