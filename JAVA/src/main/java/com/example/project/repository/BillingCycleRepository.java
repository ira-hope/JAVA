package com.example.project.repository;

/**
 * Database queries for billing cycle records.
 */

import com.example.project.entity.BillingCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingCycleRepository extends JpaRepository<BillingCycle, Long> {

	Optional<BillingCycle> findByBillingYearAndBillingMonth(int year, int month);
}
