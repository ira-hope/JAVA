package com.example.project.service.impl;

/**
 * Implements partial/full payments, penalty checks, and outstanding balance updates.
 */

import com.example.project.dto.request.PaymentCreateRequest;
import com.example.project.dto.response.PaymentResponse;
import com.example.project.entity.Bill;
import com.example.project.entity.Customer;
import com.example.project.entity.Payment;
import com.example.project.entity.enums.BillStatus;
import com.example.project.entity.enums.PaymentMethod;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.BillRepository;
import com.example.project.repository.PaymentRepository;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.BillService;
import com.example.project.service.NotificationService;
import com.example.project.service.PaymentService;
import com.example.project.util.CustomerAccessUtil;
import com.example.project.util.FinanceAccessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private static final Set<BillStatus> PAYABLE_STATUSES = EnumSet.of(
			BillStatus.PENDING,
			BillStatus.APPROVED,
			BillStatus.PARTIALLY_PAID,
			BillStatus.OVERDUE);

	private final PaymentRepository paymentRepository;
	private final BillRepository billRepository;
	private final BillService billService;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public PaymentResponse recordPayment(PaymentCreateRequest request, UserPrincipal principal) {
		FinanceAccessUtil.ensureFinanceOrAdmin(principal);
		Bill bill = billRepository.findByBillReference(request.getBillReference().trim())
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + request.getBillReference()));

		billService.applyPenaltyIfOverdue(bill);
		bill = billRepository.findById(bill.getId()).orElseThrow();

		if (!PAYABLE_STATUSES.contains(bill.getStatus())) {
			throw new BadRequestException("Bill cannot accept payments in status: " + bill.getStatus());
		}
		if (request.getAmountPaid().compareTo(bill.getOutstandingAmount()) > 0) {
			throw new BadRequestException("Payment amount exceeds outstanding balance of "
					+ bill.getOutstandingAmount());
		}

		Payment payment = Payment.builder()
				.billReference(bill.getBillReference())
				.bill(bill)
				.amountPaid(request.getAmountPaid())
				.paymentMethod(request.getPaymentMethod())
				.paymentDate(request.getPaymentDate())
				.build();
		payment = paymentRepository.save(payment);

		BigDecimal newOutstanding = bill.getOutstandingAmount().subtract(request.getAmountPaid());
		bill.setOutstandingAmount(newOutstanding);
		if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
			bill.setStatus(BillStatus.PAID);
		} else {
			bill.setStatus(BillStatus.PARTIALLY_PAID);
		}
		billRepository.save(bill);

		if (bill.getStatus() == BillStatus.PAID) {
			notifyPaymentApproved(bill);
		}

		return toResponse(payment);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<PaymentResponse> findAll(Pageable pageable) {
		Page<Payment> page = paymentRepository.findAll(pageable);
		return PagedResponse.<PaymentResponse>builder()
				.content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentResponse findById(Long id) {
		return paymentRepository.findById(id)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PaymentResponse> findByCustomerId(Long customerId, UserPrincipal principal) {
		CustomerAccessUtil.ensureCustomerOwnsResource(principal, customerId);
		return paymentRepository.findByBillMeterCustomerId(customerId).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<PaymentResponse> findByBillId(Long billId, UserPrincipal principal) {
		Bill bill = billRepository.findById(billId)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
		CustomerAccessUtil.ensureCustomerOwnsResource(principal, bill.getMeter().getCustomer().getId());
		return paymentRepository.findByBillId(billId).stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	private void notifyPaymentApproved(Bill bill) {
		Customer customer = bill.getMeter().getCustomer();
		int month = bill.getBillingCycle().getBillingMonth();
		int year = bill.getBillingCycle().getBillingYear();
		String period = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + year;
		String message = "Dear " + customer.getFullName() + ",\n"
				+ "Your " + period + " utility bill of " + bill.getTotalAmount() + " FRW has been successfully processed.";
		notificationService.notifyCustomer(customer, "WASAC Payment Approved", message);
	}

	private PaymentResponse toResponse(Payment payment) {
		return PaymentResponse.builder()
				.id(payment.getId())
				.billReference(payment.getBillReference())
				.billId(payment.getBill().getId())
				.amountPaid(payment.getAmountPaid())
				.paymentMethod(payment.getPaymentMethod())
				.paymentDate(payment.getPaymentDate())
				.createdAt(payment.getCreatedAt())
				.build();
	}
}
