package com.example.project.repository;

/**
 * Database queries for payment records.
 */

import com.example.project.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByBillId(Long billId);

	List<Payment> findByBillMeterCustomerId(Long customerId);

	void deleteByBillId(Long billId);
}
