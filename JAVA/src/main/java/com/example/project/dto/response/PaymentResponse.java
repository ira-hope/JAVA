package com.example.project.dto.response;

/**
 * API response shape for a recorded payment.
 */

import com.example.project.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

	private Long id;
	private String billReference;
	private Long billId;
	private BigDecimal amountPaid;
	private PaymentMethod paymentMethod;
	private LocalDate paymentDate;
	private LocalDateTime createdAt;
}
