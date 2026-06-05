package com.example.project.service.impl;

/**
 * Implements OTP creation, email delivery, and post-verify customer account setup.
 */

import com.example.project.dto.request.ActivateAccountRequest;
import com.example.project.dto.request.OtpRequest;
import com.example.project.dto.request.VerifyOtpRequest;
import com.example.project.entity.Customer;
import com.example.project.entity.OtpVerification;
import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.entity.User;
import com.example.project.entity.enums.RequestedRole;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.ResourceNotFoundException;
import com.example.project.repository.CustomerRepository;
import com.example.project.repository.OtpVerificationRepository;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.EmailService;
import com.example.project.service.OTPService;
import com.example.project.util.DateUtil;
import com.example.project.util.EmailUtil;
import com.example.project.util.OTPGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {

	private static final Logger log = LoggerFactory.getLogger(OTPServiceImpl.class);

	private final OtpVerificationRepository otpRepository;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final CustomerRepository customerRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.otp.expiration-minutes}")
	private int expirationMinutes;

	@Value("${app.otp.length}")
	private int otpLength;

	@Value("${app.otp.log-to-console:true}")
	private boolean logToConsole;

	@Value("${app.otp.fail-on-email-error:false}")
	private boolean failOnEmailError;

	@Override
	@Transactional
	public void generateAndSend(OtpRequest request) {
		String email = EmailUtil.normalize(request.getEmail());
		userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		saveAndSend(email);
	}

	@Override
	@Transactional
	public void resend(OtpRequest request) {
		generateAndSend(request);
	}

	@Override
	@Transactional
	public void verify(VerifyOtpRequest request) {
		String email = EmailUtil.normalize(request.getEmail());
		String otp = request.getOtp().trim();

		OtpVerification record = otpRepository
				.findTopByEmailIgnoreCaseAndVerifiedFalseOrderByCreatedAtDesc(email)
				.orElseThrow(() -> new BadRequestException(
						"No active OTP found for this email. Call POST /api/otp/send or /api/otp/resend first."));

		if (DateUtil.isExpired(record.getExpiresAt())) {
			throw new BadRequestException("OTP has expired. Request a new one via POST /api/otp/resend");
		}
		if (!record.getOtp().equals(otp)) {
			throw new BadRequestException("Invalid OTP");
		}

		record.setVerified(true);
		otpRepository.save(record);

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (hasStaffRole(user)) {
			throw new BadRequestException("OTP verification is only for customer self-registration");
		}
		if (!user.isPasswordSet()) {
			throw new BadRequestException(
					"Set your password with POST /api/auth/activate-account using the OTP from your email");
		}
		if (user.isEmailVerified() && user.getRoles().stream()
				.anyMatch(role -> role.getName() == RoleName.ROLE_CUSTOMER)) {
			throw new BadRequestException("Customer account is already verified");
		}

		Customer customer = customerRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new BadRequestException(
						"Customer profile not found. Complete registration with national ID and address first."));

		user.setEmailVerified(true);
		user.setAdminApproved(true);
		user.setRequestedRole(RequestedRole.ROLE_CUSTOMER);
		user.setRoles(AuthServiceImpl.singleRole(roleRepository, RoleName.ROLE_CUSTOMER));
		user.setCustomerId(customer.getId());
		userRepository.save(user);

		log.info("Email verified for customer {}", email);
	}

	@Override
	@Transactional
	public void sendActivationInvite(String email, String fullName) {
		String normalizedEmail = EmailUtil.normalize(email);
		saveAndSendActivation(normalizedEmail, fullName);
	}

	@Override
	@Transactional
	public void activateAccount(ActivateAccountRequest request) {
		String email = EmailUtil.normalize(request.getEmail());
		String otp = request.getOtp().trim();

		OtpVerification record = otpRepository
				.findTopByEmailIgnoreCaseAndVerifiedFalseOrderByCreatedAtDesc(email)
				.orElseThrow(() -> new BadRequestException(
						"No active OTP found. Contact WASAC support or ask admin to resend the invite."));

		if (DateUtil.isExpired(record.getExpiresAt())) {
			throw new BadRequestException("OTP has expired. Ask admin to resend the activation email.");
		}
		if (!record.getOtp().equals(otp)) {
			throw new BadRequestException("Invalid OTP");
		}

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (user.isPasswordSet() && user.isEmailVerified()) {
			throw new BadRequestException("Account is already activated");
		}
		if (user.getCustomerId() == null) {
			throw new BadRequestException("No customer profile linked to this account");
		}

		record.setVerified(true);
		otpRepository.save(record);

		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setPasswordSet(true);
		user.setEmailVerified(true);
		user.setAdminApproved(true);
		user.setRequestedRole(RequestedRole.ROLE_CUSTOMER);
		user.setRoles(AuthServiceImpl.singleRole(roleRepository, RoleName.ROLE_CUSTOMER));
		userRepository.save(user);

		log.info("Admin-invited customer activated: {}", email);
	}

	private boolean hasStaffRole(User user) {
		return user.getRoles().stream()
				.map(Role::getName)
				.anyMatch(name -> name == RoleName.ROLE_ADMIN
						|| name == RoleName.ROLE_OPERATOR
						|| name == RoleName.ROLE_FINANCE);
	}

	private void saveAndSend(String email) {
		String otp = saveOtp(email);
		if (logToConsole) {
			log.info("OTP for {}: {} (expires in {} minutes)", email, otp, expirationMinutes);
		}
		try {
			emailService.sendOtpEmail(email, otp);
		} catch (Exception ex) {
			log.warn("Failed to send OTP email to {}: {}", email, ex.getMessage());
			if (failOnEmailError) {
				throw ex;
			}
		}
	}

	private void saveAndSendActivation(String email, String fullName) {
		String otp = saveOtp(email);
		if (logToConsole) {
			log.info("Activation OTP for {}: {} (expires in {} minutes)", email, otp, expirationMinutes);
		}
		try {
			emailService.sendAccountActivationEmail(email, fullName, otp);
		} catch (Exception ex) {
			log.warn("Failed to send activation email to {}: {}", email, ex.getMessage());
			if (failOnEmailError) {
				throw ex;
			}
		}
	}

	private String saveOtp(String email) {
		String otp = OTPGenerator.generate(otpLength);
		otpRepository.save(OtpVerification.builder()
				.email(email)
				.otp(otp)
				.expiresAt(DateUtil.plusMinutes(expirationMinutes))
				.verified(false)
				.build());
		return otp;
	}
}
