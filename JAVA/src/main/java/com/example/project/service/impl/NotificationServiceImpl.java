package com.example.project.service.impl;

/**
 * Implements saving, emailing, and querying customer notifications.
 */

import com.example.project.dto.response.NotificationResponse;
import com.example.project.entity.Customer;
import com.example.project.entity.Notification;
import com.example.project.entity.enums.NotificationStatus;
import com.example.project.exception.ForbiddenException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.NotificationRepository;
import com.example.project.service.EmailService;
import com.example.project.service.NotificationService;
import com.example.project.security.UserPrincipal;
import com.example.project.util.CustomerAccessUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private final NotificationRepository notificationRepository;
	private final EmailService emailService;

	@Override
	@Transactional
	public void notifyCustomer(Customer customer, String subject, String message) {
		Notification notification = Notification.builder()
				.customer(customer)
				.message(message)
				.recipientEmail(customer.getEmail())
				.status(NotificationStatus.PENDING)
				.read(false)
				.build();
		notificationRepository.save(notification);

		try {
			emailService.sendPlainEmail(customer.getEmail(), subject, message);
			notification.setStatus(NotificationStatus.SENT);
			notificationRepository.save(notification);
		} catch (Exception ex) {
			log.warn("Failed to email customer {}: {}", customer.getEmail(), ex.getMessage());
			notification.setStatus(NotificationStatus.FAILED);
			notificationRepository.save(notification);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationResponse> findAll() {
		return notificationRepository.findAll().stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationResponse> findByCustomerId(Long customerId, UserPrincipal principal) {
		CustomerAccessUtil.ensureCustomerOwnsResource(principal, customerId);
		return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public NotificationResponse markAsRead(Long id, UserPrincipal principal) {
		Notification notification = notificationRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
		if (CustomerAccessUtil.isCustomer(principal)
				&& (principal.getCustomerId() == null
				|| !principal.getCustomerId().equals(notification.getCustomer().getId()))) {
			throw new ForbiddenException("You can only update your own notifications");
		}
		notification.setRead(true);
		return toResponse(notificationRepository.save(notification));
	}

	private NotificationResponse toResponse(Notification notification) {
		return NotificationResponse.builder()
				.id(notification.getId())
				.customerId(notification.getCustomer().getId())
				.message(notification.getMessage())
				.status(notification.getStatus())
				.recipientEmail(notification.getRecipientEmail())
				.read(notification.isRead())
				.createdAt(notification.getCreatedAt())
				.build();
	}
}
