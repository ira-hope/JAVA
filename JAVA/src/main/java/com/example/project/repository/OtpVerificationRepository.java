package com.example.project.repository;

/**
 * Database queries for OTP verification records.
 */

import com.example.project.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

	Optional<OtpVerification> findTopByEmailIgnoreCaseAndVerifiedFalseOrderByCreatedAtDesc(String email);
}
