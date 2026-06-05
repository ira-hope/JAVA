package com.example.project.repository;

/**
 * Database queries for meter records.
 */

import com.example.project.entity.Meter;
import com.example.project.entity.enums.MeterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeterRepository extends JpaRepository<Meter, Long> {

	Optional<Meter> findByMeterNumber(String meterNumber);

	boolean existsByMeterNumber(String meterNumber);

	List<Meter> findByCustomerId(Long customerId);

	List<Meter> findByStatus(MeterStatus status);
}
