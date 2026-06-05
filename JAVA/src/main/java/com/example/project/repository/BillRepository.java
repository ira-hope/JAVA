package com.example.project.repository;

/**
 * Database queries for bill records.
 */

import com.example.project.entity.Bill;
import com.example.project.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

	Optional<Bill> findByBillReference(String billReference);

	List<Bill> findByMeterCustomerId(Long customerId);

	List<Bill> findByMeterId(Long meterId);

	List<Bill> findByStatus(BillStatus status);

	boolean existsByMeterIdAndBillingCycleId(Long meterId, Long billingCycleId);

	boolean existsByMeterReadingId(Long meterReadingId);
}
