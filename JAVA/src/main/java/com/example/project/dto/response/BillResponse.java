package com.example.project.dto.response;

/**
 * API response shape for a generated utility bill.
 */

import com.example.project.entity.enums.BillStatus;
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
public class BillResponse {

	private Long id;
	private String billReference;
	private Long meterId;
	private String meterNumber;
	private Long customerId;
	private String customerName;
	private int billingYear;
	private int billingMonth;
	private Long tariffVersionId;
	private BigDecimal usageUnits;
	private BigDecimal fixedCharge;
	private BigDecimal subtotal;
	private BigDecimal vatAmount;
	private BigDecimal penaltyAmount;
	private BigDecimal totalAmount;
	private BigDecimal outstandingAmount;
	private BillStatus status;
	private LocalDate dueDate;
	private LocalDateTime createdAt;
}
