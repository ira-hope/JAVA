package com.example.project.repository;

import com.example.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailAndDeletedFalse(String email);

	boolean existsByEmailAndDeletedFalse(String email);

	Optional<User> findByIdAndDeletedFalse(Long id);

	Page<User> findByDeletedFalse(Pageable pageable);

	Page<User> findByDeletedFalseAndEmailContainingIgnoreCase(String email, Pageable pageable);
}
