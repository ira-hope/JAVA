package com.example.project.dto.request;

/**
 * Request body for installing a meter on a customer account.
 */

import com.example.project.entity.enums.MeterStatus;
import com.example.project.entity.enums.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MeterCreateRequest {

	@NotBlank(message = "Meter number is required")
	@Size(max = 50)
	private String meterNumber;

	@NotNull(message = "Meter type is required")
	private MeterType meterType;

	@NotNull(message = "Installation date is required")
	private LocalDate installationDate;

	@NotNull(message = "Customer ID is required")
	private Long customerId;

	private MeterStatus status;
}
