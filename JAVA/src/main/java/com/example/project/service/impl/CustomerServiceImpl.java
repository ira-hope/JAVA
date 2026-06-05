package com.example.project.service.impl;

/**
 * Implements customer self-service profile lookup and updates.
 */

import com.example.project.dto.request.CustomerCreateRequest;
import com.example.project.dto.request.CustomerUpdateRequest;
import com.example.project.dto.response.CustomerResponse;
import com.example.project.entity.Bill;
import com.example.project.entity.Customer;
import com.example.project.entity.Meter;
import com.example.project.entity.User;
import com.example.project.entity.enums.CustomerStatus;
import com.example.project.entity.enums.RequestedRole;
import com.example.project.entity.enums.UserStatus;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.DuplicateResourceException;
import com.example.project.exception.ForbiddenException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.BillRepository;
import com.example.project.repository.CustomerRepository;
import com.example.project.repository.MeterReadingRepository;
import com.example.project.repository.MeterRepository;
import com.example.project.repository.NotificationRepository;
import com.example.project.repository.PaymentRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.UserPrincipal;
import com.example.project.service.AuditService;
import com.example.project.service.CustomerService;
import com.example.project.service.OTPService;
import com.example.project.util.CustomerAccessUtil;
import com.example.project.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final MeterRepository meterRepository;
	private final MeterReadingRepository meterReadingRepository;
	private final BillRepository billRepository;
	private final PaymentRepository paymentRepository;
	private final NotificationRepository notificationRepository;
	private final PasswordEncoder passwordEncoder;
	private final OTPService otpService;
	private final AuditService auditService;

	@Override
	@Transactional
	public CustomerResponse createByAdmin(CustomerCreateRequest request, String performedBy) {
		String email = EmailUtil.normalize(request.getEmail());
		String phone = request.getPhone().trim();
		String nationalId = request.getNationalId().trim();

		if (customerRepository.existsByNationalId(nationalId)) {
			throw new DuplicateResourceException("Customer with this national ID already exists");
		}
		if (customerRepository.existsByEmail(email)) {
			throw new DuplicateResourceException("Customer with this email already exists");
		}
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateResourceException("Email already registered to a user account");
		}
		if (userRepository.existsByPhone(phone)) {
			throw new DuplicateResourceException("Phone number already registered");
		}

		Customer customer = Customer.builder()
				.fullName(request.getFullName().trim())
				.nationalId(nationalId)
				.email(email)
				.phone(phone)
				.address(request.getAddress().trim())
				.status(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE)
				.build();
		customer = customerRepository.save(customer);

		User user = User.builder()
				.fullName(customer.getFullName())
				.email(email)
				.phone(phone)
				.password(passwordEncoder.encode(UUID.randomUUID().toString()))
				.status(UserStatus.ACTIVE)
				.emailVerified(false)
				.passwordSet(false)
				.requestedRole(RequestedRole.ROLE_CUSTOMER)
				.adminApproved(false)
				.customerId(customer.getId())
				.roles(new HashSet<>())
				.build();
		userRepository.save(user);

		otpService.sendActivationInvite(email, customer.getFullName());
		auditService.log("CREATE_CUSTOMER", "Customer", customer.getId(), performedBy,
				"Admin created customer and sent activation email to " + email);

		return toResponse(customer);
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerResponse getMyProfile(UserPrincipal principal) {
		return toResponse(getCustomerForPrincipal(principal));
	}

	@Override
	@Transactional
	public CustomerResponse updateMyProfile(UserPrincipal principal, CustomerUpdateRequest request) {
		if (!CustomerAccessUtil.isCustomer(principal)) {
			throw new ForbiddenException("Only customers can update their profile");
		}
		Customer customer = getCustomerForPrincipal(principal);
		User user = userRepository.findByEmailIgnoreCase(principal.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (request.getFullName() != null) {
			customer.setFullName(request.getFullName().trim());
			user.setFullName(request.getFullName().trim());
		}
		if (request.getEmail() != null) {
			String email = EmailUtil.normalize(request.getEmail());
			customerRepository.findByEmailIgnoreCase(email)
					.filter(existing -> !existing.getId().equals(customer.getId()))
					.ifPresent(existing -> {
						throw new DuplicateResourceException("Customer with this email already exists");
					});
			userRepository.findByEmailIgnoreCase(email)
					.filter(existing -> !existing.getId().equals(user.getId()))
					.ifPresent(existing -> {
						throw new DuplicateResourceException("Email already registered to another user");
					});
			customer.setEmail(email);
			user.setEmail(email);
		}
		if (request.getPhone() != null) {
			customer.setPhone(request.getPhone().trim());
			user.setPhone(request.getPhone().trim());
		}
		if (request.getAddress() != null) {
			customer.setAddress(request.getAddress().trim());
		}
		if (request.getStatus() != null) {
			throw new BadRequestException("Customers cannot change their own account status");
		}

		userRepository.save(user);
		return toResponse(customerRepository.save(customer));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
		List<Meter> meters = meterRepository.findByCustomerId(id);
		for (Meter meter : meters) {
			List<Bill> bills = billRepository.findByMeterId(meter.getId());
			for (Bill bill : bills) {
				paymentRepository.deleteByBillId(bill.getId());
				billRepository.delete(bill);
			}
			meterReadingRepository.deleteByMeterId(meter.getId());
			meterRepository.delete(meter);
		}
		notificationRepository.deleteByCustomerId(id);
		userRepository.findByEmailIgnoreCase(customer.getEmail()).ifPresent(userRepository::delete);
		customerRepository.delete(customer);
	}

	private Customer getCustomerForPrincipal(UserPrincipal principal) {
		if (principal == null || principal.getCustomerId() == null) {
			throw new BadRequestException("No customer profile linked to this account");
		}
		return customerRepository.findById(principal.getCustomerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
	}

	private CustomerResponse toResponse(Customer customer) {
		return CustomerResponse.builder()
				.id(customer.getId())
				.fullName(customer.getFullName())
				.nationalId(customer.getNationalId())
				.email(customer.getEmail())
				.phone(customer.getPhone())
				.address(customer.getAddress())
				.status(customer.getStatus())
				.createdAt(customer.getCreatedAt())
				.updatedAt(customer.getUpdatedAt())
				.build();
	}
}
