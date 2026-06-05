package com.example.project.entity.enums;

/**
 * Allowed payment states for a bill in the WASAC billing workflow.
 */

public enum BillStatus {
	PENDING,
	APPROVED,
	PARTIALLY_PAID,
	PAID,
	OVERDUE
}
