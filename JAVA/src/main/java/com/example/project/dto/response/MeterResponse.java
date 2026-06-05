package com.example.project.dto.response;

/**
 * API response shape for meter installation details.
 */

import com.example.project.entity.enums.MeterStatus;
import com.example.project.entity.enums.MeterType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeterResponse {

	private Long id;
	private String meterNumber;
	private MeterType meterType;
	private LocalDate installationDate;
	private MeterStatus status;
	private Long customerId;
	private String customerName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
