package com.example.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification extends BaseEntity {

	@Column(nullable = false)
	private String email;

	@Column(nullable = false, length = 10)
	private String otp;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	@Builder.Default
	private boolean verified = false;
}
