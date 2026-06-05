package com.example.project.repository;

/**
 * Database queries for customer records.
 */

import com.example.project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	boolean existsByNationalId(String nationalId);

	boolean existsByEmail(String email);

	Optional<Customer> findByNationalId(String nationalId);

	Optional<Customer> findByEmailIgnoreCase(String email);
}
