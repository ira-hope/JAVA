package com.example.project.dto.request;

/**
 * Request body for recording a payment against a bill.
 */

import com.example.project.entity.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentCreateRequest {

	@NotBlank(message = "Bill reference is required")
	private String billReference;

	@NotNull(message = "Amount paid is required")
	@DecimalMin(value = "0.01", message = "Amount paid must be greater than zero")
	private BigDecimal amountPaid;

	@NotNull(message = "Payment method is required")
	private PaymentMethod paymentMethod;

	@NotNull(message = "Payment date is required")
	private LocalDate paymentDate;
}
