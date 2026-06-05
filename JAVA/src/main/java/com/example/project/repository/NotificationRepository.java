package com.example.project.repository;

/**
 * Database queries for notification records.
 */

import com.example.project.entity.Notification;
import com.example.project.entity.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByStatus(NotificationStatus status);

	List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

	void deleteByCustomerId(Long customerId);
}
