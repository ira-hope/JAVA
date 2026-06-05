package com.example.project.repository;

/**
 * Database queries for tariff tier records.
 */

import com.example.project.entity.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TariffTierRepository extends JpaRepository<TariffTier, Long> {

	List<TariffTier> findByTariffVersionIdOrderByTierFromAsc(Long tariffVersionId);
}
