package com.example.project.service;

/**
 * Defines customer notification delivery and lookup operations.
 */

import com.example.project.dto.response.NotificationResponse;
import com.example.project.entity.Customer;
import com.example.project.security.UserPrincipal;

import java.util.List;

public interface NotificationService {

	void notifyCustomer(Customer customer, String subject, String message);

	List<NotificationResponse> findAll();

	List<NotificationResponse> findByCustomerId(Long customerId, UserPrincipal principal);

	NotificationResponse markAsRead(Long id, UserPrincipal principal);
}
