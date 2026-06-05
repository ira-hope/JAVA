package com.example.project.service;

/**
 * Defines payment recording and lookup operations.
 */

import com.example.project.dto.request.PaymentCreateRequest;
import com.example.project.dto.response.PaymentResponse;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

	PaymentResponse recordPayment(PaymentCreateRequest request, UserPrincipal principal);

	PagedResponse<PaymentResponse> findAll(Pageable pageable);

	PaymentResponse findById(Long id);

	List<PaymentResponse> findByCustomerId(Long customerId, UserPrincipal principal);

	List<PaymentResponse> findByBillId(Long billId, UserPrincipal principal);
}
