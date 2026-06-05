package com.example.project.repository;

/**
 * Database queries for meter reading records.
 */

import com.example.project.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

	boolean existsByMeterIdAndReadingYearAndReadingMonth(Long meterId, int year, int month);

	Optional<MeterReading> findByMeterIdAndReadingYearAndReadingMonth(Long meterId, int year, int month);

	Optional<MeterReading> findTopByMeterIdOrderByReadingDateDesc(Long meterId);

	List<MeterReading> findByMeterIdOrderByReadingDateDesc(Long meterId);

	void deleteByMeterId(Long meterId);
}
