package com.example.project.service;

public interface AuditService {

	void log(String action, String entityType, Long entityId, String performedBy, String details);
}
