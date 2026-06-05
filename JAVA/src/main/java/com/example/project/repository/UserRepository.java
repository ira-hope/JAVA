package com.example.project.repository;

/**
 * Database queries for user records.
 */

import com.example.project.entity.User;
import com.example.project.entity.enums.RequestedRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	@EntityGraph(attributePaths = {"roles"})
	Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = {"roles"})
	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmail(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByPhone(String phone);

	@EntityGraph(attributePaths = {"roles"})
	List<User> findByEmailVerifiedTrueAndAdminApprovedFalseAndRequestedRoleIn(List<RequestedRole> roles);

	@Override
	@EntityGraph(attributePaths = {"roles"})
	Optional<User> findById(Long id);
}
