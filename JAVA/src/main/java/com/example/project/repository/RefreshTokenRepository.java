package com.example.project.repository;

/**
 * Database queries for refresh token records.
 */

import com.example.project.entity.RefreshToken;
import com.example.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

	void deleteByUser(User user);

	void deleteByUserAndRevokedTrue(User user);
}
