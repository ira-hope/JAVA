package com.example.project.service;

/**
 * Defines operations for writing audit trail entries.
 */

public interface AuditService {

	void log(String action, String entityType, Long entityId, String performedBy, String details);
}
