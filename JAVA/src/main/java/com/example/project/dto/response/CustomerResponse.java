package com.example.project.dto.response;

/**
 * API response shape for customer details.
 */

import com.example.project.entity.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

	private Long id;
	private String fullName;
	private String nationalId;
	private String email;
	private String phone;
	private String address;
	private CustomerStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
