package com.example.project.service.impl;

/**
 * Implements per-reading bill generation, approval workflow, and overdue penalties.
 */

import com.example.project.dto.response.BillResponse;
import com.example.project.entity.Bill;
import com.example.project.entity.BillingCycle;
import com.example.project.entity.Customer;
import com.example.project.entity.Meter;
import com.example.project.entity.MeterReading;
import com.example.project.entity.TariffTier;
import com.example.project.entity.TariffVersion;
import com.example.project.entity.enums.BillStatus;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.BillRepository;
import com.example.project.repository.BillingCycleRepository;
import com.example.project.repository.MeterReadingRepository;
import com.example.project.repository.TariffTierRepository;
import com.example.project.repository.TariffVersionRepository;
import com.example.project.response.PagedResponse;
import com.example.project.security.UserPrincipal;
import com.example.project.service.BillService;
import com.example.project.service.NotificationService;
import com.example.project.util.CustomerAccessUtil;
import com.example.project.util.FinanceAccessUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

	private final BillingCycleRepository billingCycleRepository;
	private final MeterReadingRepository meterReadingRepository;
	private final TariffVersionRepository tariffVersionRepository;
	private final TariffTierRepository tariffTierRepository;
	private final BillRepository billRepository;
	private final NotificationService notificationService;

	@Override
	@Transactional
	public BillResponse generateFromReading(Long readingId) {
		MeterReading reading = meterReadingRepository.findById(readingId)
				.orElseThrow(() -> new ResourceNotFoundException("Meter reading not found: " + readingId));

		if (billRepository.existsByMeterReadingId(readingId)) {
			throw new BadRequestException("A bill already exists for this reading");
		}

		Meter meter = reading.getMeter();
		int year = reading.getReadingYear();
		int month = reading.getReadingMonth();
		BillingCycle cycle = getOrCreateCycle(year, month);

		TariffVersion tariff = tariffVersionRepository
				.findTopByMeterTypeAndEffectiveFromLessThanEqualOrderByEffectiveFromDescVersionNumberDesc(
						meter.getMeterType(), reading.getReadingDate())
				.orElseThrow(() -> new BadRequestException(
						"No active tariff found for meter type " + meter.getMeterType()));

		List<TariffTier> tiers = tariffTierRepository.findByTariffVersionIdOrderByTierFromAsc(tariff.getId());
		BigDecimal usageUnits = reading.getCurrentReading().subtract(reading.getPreviousReading());
		BigDecimal usageCharge = calculateUsageCharge(usageUnits, tiers);
		BigDecimal fixedCharge = tariff.getFixedServiceCharge();
		BigDecimal subtotal = fixedCharge.add(usageCharge).setScale(2, RoundingMode.HALF_UP);
		BigDecimal vatAmount = subtotal.multiply(tariff.getVatRate())
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal totalAmount = subtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

		String billReference = "WASAC-" + year + String.format("%02d", month)
				+ "-" + meter.getMeterNumber() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

		Bill bill = Bill.builder()
				.billReference(billReference)
				.meter(meter)
				.billingCycle(cycle)
				.tariffVersion(tariff)
				.meterReadingId(readingId)
				.usageUnits(usageUnits)
				.fixedCharge(fixedCharge)
				.subtotal(subtotal)
				.vatAmount(vatAmount)
				.penaltyAmount(BigDecimal.ZERO)
				.totalAmount(totalAmount)
				.outstandingAmount(totalAmount)
				.status(BillStatus.PENDING)
				.dueDate(reading.getReadingDate().plusDays(15))
				.build();

		bill = billRepository.save(bill);
		return toResponse(bill);
	}

	@Override
	@Transactional
	public BillResponse approve(Long billId, UserPrincipal principal) {
		FinanceAccessUtil.ensureFinanceOrAdmin(principal);
		Bill bill = getBill(billId);
		if (bill.getStatus() != BillStatus.PENDING) {
			throw new BadRequestException("Only PENDING bills can be approved");
		}
		bill.setStatus(BillStatus.APPROVED);
		bill = billRepository.save(bill);
		notifyBillApproved(bill);
		return toResponse(bill);
	}

	@Override
	@Transactional
	public BillResponse findById(Long id, UserPrincipal principal) {
		Bill bill = getBill(id);
		applyPenaltyIfOverdue(bill);
		CustomerAccessUtil.ensureCustomerOwnsResource(principal, bill.getMeter().getCustomer().getId());
		return toResponse(bill);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<BillResponse> findAll(Pageable pageable) {
		Page<Bill> page = billRepository.findAll(pageable);
		return toPagedResponse(page);
	}

	@Override
	@Transactional
	public List<BillResponse> findByCustomerId(Long customerId, UserPrincipal principal) {
		CustomerAccessUtil.ensureCustomerOwnsResource(principal, customerId);
		return billRepository.findByMeterCustomerId(customerId).stream()
				.map(bill -> {
					applyPenaltyIfOverdue(bill);
					return toResponse(bill);
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void applyPenaltyIfOverdue(Bill bill) {
		if (bill.getStatus() != BillStatus.APPROVED && bill.getStatus() != BillStatus.PARTIALLY_PAID) {
			return;
		}
		if (!LocalDate.now().isAfter(bill.getDueDate())) {
			return;
		}
		if (bill.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0) {
			bill.setStatus(BillStatus.OVERDUE);
			billRepository.save(bill);
			return;
		}

		BigDecimal penaltyRate = bill.getTariffVersion().getLatePenaltyRate();
		BigDecimal penalty = bill.getOutstandingAmount()
				.multiply(penaltyRate)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		bill.setPenaltyAmount(penalty);
		bill.setTotalAmount(bill.getTotalAmount().add(penalty));
		bill.setOutstandingAmount(bill.getOutstandingAmount().add(penalty));
		bill.setStatus(BillStatus.OVERDUE);
		billRepository.save(bill);
	}

	private BillingCycle getOrCreateCycle(int year, int month) {
		return billingCycleRepository.findByBillingYearAndBillingMonth(year, month)
				.orElseGet(() -> {
					YearMonth ym = YearMonth.of(year, month);
					BillingCycle cycle = BillingCycle.builder()
							.billingYear(year)
							.billingMonth(month)
							.cycleStart(ym.atDay(1))
							.cycleEnd(ym.atEndOfMonth())
							.build();
					return billingCycleRepository.save(cycle);
				});
	}

	private BigDecimal calculateUsageCharge(BigDecimal usageUnits, List<TariffTier> tiers) {
		if (tiers.isEmpty()) {
			throw new BadRequestException("Tariff has no pricing tiers configured");
		}
		BigDecimal remaining = usageUnits;
		BigDecimal charge = BigDecimal.ZERO;
		for (TariffTier tier : tiers) {
			if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}
			BigDecimal tierSpan = tier.getTierTo().subtract(tier.getTierFrom());
			BigDecimal unitsInTier = remaining.min(tierSpan);
			if (unitsInTier.compareTo(BigDecimal.ZERO) > 0) {
				charge = charge.add(unitsInTier.multiply(tier.getRatePerUnit()));
				remaining = remaining.subtract(unitsInTier);
			}
		}
		if (remaining.compareTo(BigDecimal.ZERO) > 0) {
			TariffTier lastTier = tiers.get(tiers.size() - 1);
			charge = charge.add(remaining.multiply(lastTier.getRatePerUnit()));
		}
		return charge.setScale(2, RoundingMode.HALF_UP);
	}

	private void notifyBillGenerated(Bill bill) {
		Customer customer = bill.getMeter().getCustomer();
		String message = "Dear " + customer.getFullName() + ",\n\n"
				+ "Your utility bill has been generated.\n"
				+ "Bill reference: " + bill.getBillReference() + "\n"
				+ "Amount: " + bill.getTotalAmount() + " FRW\n"
				+ "Usage: " + bill.getUsageUnits() + " units\n"
				+ "Due date: " + bill.getDueDate() + "\n\n"
				+ "Please pay at WASAC. Finance will record your payment once it is received.";
		notificationService.notifyCustomer(customer, "WASAC Bill Generated", message);
	}

	private void notifyBillApproved(Bill bill) {
		Customer customer = bill.getMeter().getCustomer();
		String message = "Dear " + customer.getFullName() + ",\n\n"
				+ "Your utility bill has been approved.\n"
				+ "Bill reference: " + bill.getBillReference() + "\n"
				+ "Amount due: " + bill.getOutstandingAmount() + " FRW\n"
				+ "Due date: " + bill.getDueDate() + "\n\n"
				+ "Please settle your bill at WASAC.";
		notificationService.notifyCustomer(customer, "WASAC Bill Approved", message);
	}

	private Bill getBill(Long id) {
		return billRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
	}

	private BillResponse toResponse(Bill bill) {
		return BillResponse.builder()
				.id(bill.getId())
				.billReference(bill.getBillReference())
				.meterId(bill.getMeter().getId())
				.meterNumber(bill.getMeter().getMeterNumber())
				.customerId(bill.getMeter().getCustomer().getId())
				.customerName(bill.getMeter().getCustomer().getFullName())
				.billingYear(bill.getBillingCycle().getBillingYear())
				.billingMonth(bill.getBillingCycle().getBillingMonth())
				.tariffVersionId(bill.getTariffVersion().getId())
				.usageUnits(bill.getUsageUnits())
				.fixedCharge(bill.getFixedCharge())
				.subtotal(bill.getSubtotal())
				.vatAmount(bill.getVatAmount())
				.penaltyAmount(bill.getPenaltyAmount())
				.totalAmount(bill.getTotalAmount())
				.outstandingAmount(bill.getOutstandingAmount())
				.status(bill.getStatus())
				.dueDate(bill.getDueDate())
				.createdAt(bill.getCreatedAt())
				.build();
	}

	private PagedResponse<BillResponse> toPagedResponse(Page<Bill> page) {
		return PagedResponse.<BillResponse>builder()
				.content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
				.page(page.getNumber())
				.size(page.getSize())
				.totalElements(page.getTotalElements())
				.totalPages(page.getTotalPages())
				.build();
	}
}
