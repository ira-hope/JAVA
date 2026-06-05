package com.example.project.service;

/**
 * Defines bill generation, approval, penalty, and lookup operations.
 */

import com.example.project.dto.response.BillResponse;
import com.example.project.entity.Bill;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BillService {

	BillResponse generateFromReading(Long readingId);

	BillResponse approve(Long billId, UserPrincipal principal);

	BillResponse findById(Long id, UserPrincipal principal);

	PagedResponse<BillResponse> findAll(Pageable pageable);

	List<BillResponse> findByCustomerId(Long customerId, UserPrincipal principal);

	void applyPenaltyIfOverdue(Bill bill);
}
