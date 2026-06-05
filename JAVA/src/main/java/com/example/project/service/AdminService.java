package com.example.project.service;

/**
 * Defines admin operations such as viewing audit logs.
 */

import com.example.project.dto.response.AuditLogResponse;
import com.example.project.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface AdminService {

	PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable);
}
