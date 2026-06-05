package com.example.project.repository;

/**
 * Database queries for tariff version records.
 */

import com.example.project.entity.TariffVersion;
import com.example.project.entity.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TariffVersionRepository extends JpaRepository<TariffVersion, Long> {

	List<TariffVersion> findByMeterTypeOrderByVersionNumberDesc(MeterType meterType);

	Optional<TariffVersion> findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionNumberDesc(
			MeterType meterType, LocalDate date);
}
