package com.example.project.service.impl;

/**
 * Implements registration, login, lockout, and token refresh logic.
 */

import com.example.project.dto.request.ActivateAccountRequest;
import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.OtpRequest;
import com.example.project.dto.request.RefreshTokenRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.Customer;
import com.example.project.entity.RefreshToken;
import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.entity.User;
import com.example.project.entity.enums.CustomerStatus;
import com.example.project.entity.enums.RequestedRole;
import com.example.project.entity.enums.UserStatus;
import com.example.project.exception.AccountLockedException;
import com.example.project.exception.BadRequestException;
import com.example.project.exception.DuplicateResourceException;
import com.example.project.exception.ForbiddenException;
import com.example.project.exception.UnauthorizedException;
import com.example.project.mapper.UserMapper;
import com.example.project.repository.CustomerRepository;
import com.example.project.repository.RefreshTokenRepository;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtil;
import com.example.project.security.UserPrincipal;
import com.example.project.service.AuditService;
import com.example.project.service.AuthService;
import com.example.project.service.OTPService;
import com.example.project.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
	private static final Set<String> STAFF_ROLES = Set.of("ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FINANCE");

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final RoleRepository roleRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserMapper userMapper;
	private final OTPService otpService;
	private final AuditService auditService;

	@Value("${app.security.max-login-attempts}")
	private int maxLoginAttempts;

	@Value("${app.security.lockout-duration-minutes}")
	private int lockoutDurationMinutes;

	@Override
	@Transactional
	public String register(RegisterRequest request, UserPrincipal principal) {
		String email = EmailUtil.normalize(request.getEmail());
		String phone = request.getPhone().trim();
		String role = normalizeRole(request.getRole());

		if (role != null) {
			registerStaff(request, principal, email, phone, role);
			return "Staff account created successfully. The user can log in immediately.";
		}
		if (request.getRole() != null && !request.getRole().isBlank()) {
			throw new ForbiddenException("Only administrators can register staff accounts with a role");
		}
		return registerCustomer(request, email, phone);
	}

	private void registerStaff(RegisterRequest request, UserPrincipal principal, String email, String phone, String role) {
		if (principal == null || !principal.getRoleNames().contains("ROLE_ADMIN")) {
			throw new ForbiddenException("Only administrators can register staff accounts");
		}
		if (!STAFF_ROLES.contains(role)) {
			throw new BadRequestException("Invalid staff role. Use ROLE_ADMIN, ROLE_OPERATOR, or ROLE_FINANCE");
		}
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateResourceException("Email already registered");
		}
		if (userRepository.existsByPhone(phone)) {
			throw new DuplicateResourceException("Phone number already registered");
		}

		RoleName roleName = RoleName.valueOf(role);
		User user = User.builder()
				.fullName(request.getFullName().trim())
				.email(email)
				.phone(phone)
				.password(passwordEncoder.encode(request.getPassword()))
				.status(UserStatus.ACTIVE)
				.emailVerified(true)
				.requestedRole(mapRequestedRole(roleName))
				.adminApproved(true)
				.customerId(null)
				.roles(singleRole(roleRepository, roleName))
				.build();

		user = userRepository.save(user);
		log.info("Staff user registered by admin: {} as {}", user.getEmail(), role);
		auditService.log("REGISTER_STAFF", "User", user.getId(), principal.getEmail(),
				"Staff registered with role " + role);
	}

	private String registerCustomer(RegisterRequest request, String email, String phone) {
		validateCustomerRegistrationFields(request);

		User existing = userRepository.findByEmailIgnoreCase(email).orElse(null);
		if (existing != null) {
			if (!existing.isEmailVerified()) {
				saveCustomerProfile(request, email, phone);
				resendCustomerOtp(email);
				return "OTP resent to your email. Verify it with POST /api/otp/verify before logging in.";
			}
			throw new DuplicateResourceException("Email already registered");
		}
		if (userRepository.existsByPhone(phone)) {
			throw new DuplicateResourceException("Phone number already registered");
		}

		User user = User.builder()
				.fullName(request.getFullName().trim())
				.email(email)
				.phone(phone)
				.password(passwordEncoder.encode(request.getPassword()))
				.status(UserStatus.ACTIVE)
				.emailVerified(false)
				.passwordSet(true)
				.requestedRole(RequestedRole.ROLE_CUSTOMER)
				.adminApproved(false)
				.roles(new HashSet<>())
				.build();

		user = userRepository.save(user);
		saveCustomerProfile(request, email, phone);
		log.info("Customer registered: {}", user.getEmail());
		auditService.log("REGISTER", "User", user.getId(), user.getEmail(), "Customer self-registration");

		resendCustomerOtp(user.getEmail());
		return "Registration successful. An OTP has been sent to your email. "
				+ "Verify it with POST /api/otp/verify before logging in.";
	}

	private void validateCustomerRegistrationFields(RegisterRequest request) {
		if (request.getNationalId() == null || request.getNationalId().isBlank()) {
			throw new BadRequestException("National ID is required");
		}
		if (request.getAddress() == null || request.getAddress().isBlank()) {
			throw new BadRequestException("Address is required");
		}
	}

	private void saveCustomerProfile(RegisterRequest request, String email, String phone) {
		String nationalId = request.getNationalId().trim();
		customerRepository.findByNationalId(nationalId).ifPresent(existing -> {
			if (!existing.getEmail().equalsIgnoreCase(email)) {
				throw new DuplicateResourceException("National ID already registered");
			}
		});

		Customer customer = customerRepository.findByEmailIgnoreCase(email)
				.orElse(Customer.builder().build());
		customer.setFullName(request.getFullName().trim());
		customer.setNationalId(nationalId);
		customer.setEmail(email);
		customer.setPhone(phone);
		customer.setAddress(request.getAddress().trim());
		customer.setStatus(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE);
		customerRepository.save(customer);
	}

	private void resendCustomerOtp(String email) {
		OtpRequest otpRequest = new OtpRequest();
		otpRequest.setEmail(email);
		otpService.generateAndSend(otpRequest);
	}

	@Override
	@Transactional
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmailIgnoreCase(request.getEmail())
				.orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

		unlockIfExpired(user);
		if (isAccountLocked(user)) {
			throw new AccountLockedException("Account is locked. Try again after " + user.getAccountLockedUntil());
		}
		if (!user.isPasswordSet()) {
			throw new ForbiddenException(
					"Account not activated. Set your password with POST /api/auth/activate-account.");
		}
		if (!user.isEmailVerified()) {
			throw new ForbiddenException("Email not verified. Verify OTP before login.");
		}
		if (user.getRoles().isEmpty()) {
			throw new ForbiddenException("Account has no assigned role. Contact WASAC support.");
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
			UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
			resetFailedAttempts(user);
			log.info("User logged in: {}", userPrincipal.getEmail());
			auditService.log("LOGIN", "User", user.getId(), user.getEmail(), "Successful login");
			return buildAuthResponse(userPrincipal, user);
		} catch (LockedException ex) {
			throw new AccountLockedException("Account is locked");
		} catch (BadCredentialsException ex) {
			handleFailedLogin(user);
			throw new UnauthorizedException("Invalid email or password");
		}
	}

	@Override
	@Transactional
	public void activateAccount(ActivateAccountRequest request) {
		otpService.activateAccount(request);
	}

	@Override
	@Transactional
	public AuthResponse refresh(RefreshTokenRequest request) {
		String token = request.getRefreshToken();
		if (!jwtUtil.isRefreshToken(token)) {
			throw new UnauthorizedException("Invalid refresh token");
		}

		RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(token)
				.orElseThrow(() -> new UnauthorizedException("Refresh token revoked or not found"));

		String email = jwtUtil.extractUsername(token);
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new UnauthorizedException("User not found"));
		UserPrincipal userPrincipal = new UserPrincipal(user);

		if (!jwtUtil.isTokenValid(token, userPrincipal)) {
			storedToken.setRevoked(true);
			refreshTokenRepository.save(storedToken);
			throw new UnauthorizedException("Refresh token expired");
		}

		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);
		auditService.log("TOKEN_REFRESH", "User", user.getId(), user.getEmail(), "Refresh token rotated");
		return buildAuthResponse(userPrincipal, user);
	}

	private AuthResponse buildAuthResponse(UserDetails userDetails, User user) {
		UserResponse userResponse = userMapper.toResponse(user);
		String accessToken = jwtUtil.generateAccessToken(userDetails);
		String refreshToken = jwtUtil.generateRefreshToken(userDetails);
		persistRefreshToken(user, refreshToken);
		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.tokenType("Bearer")
				.user(userResponse)
				.build();
	}

	private void persistRefreshToken(User user, String token) {
		refreshTokenRepository.deleteByUserAndRevokedTrue(user);
		refreshTokenRepository.save(RefreshToken.builder()
				.token(token)
				.user(user)
				.expiresAt(jwtUtil.extractExpirationAsLocalDateTime(token))
				.build());
	}

	private void handleFailedLogin(User user) {
		int attempts = user.getFailedLoginAttempts() + 1;
		user.setFailedLoginAttempts(attempts);
		if (attempts >= maxLoginAttempts) {
			user.setAccountLocked(true);
			user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
			auditService.log("ACCOUNT_LOCKED", "User", user.getId(), user.getEmail(),
					"Account locked after " + attempts + " failed attempts");
		}
		userRepository.save(user);
	}

	private void resetFailedAttempts(User user) {
		if (user.getFailedLoginAttempts() > 0 || user.isAccountLocked()) {
			user.setFailedLoginAttempts(0);
			user.setAccountLocked(false);
			user.setAccountLockedUntil(null);
			userRepository.save(user);
		}
	}

	private void unlockIfExpired(User user) {
		if (user.isAccountLocked()
				&& user.getAccountLockedUntil() != null
				&& user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
			user.setAccountLocked(false);
			user.setAccountLockedUntil(null);
			user.setFailedLoginAttempts(0);
			userRepository.save(user);
		}
	}

	private boolean isAccountLocked(User user) {
		return user.isAccountLocked()
				&& (user.getAccountLockedUntil() == null || user.getAccountLockedUntil().isAfter(LocalDateTime.now()));
	}

	private String normalizeRole(String role) {
		if (role == null || role.isBlank()) {
			return null;
		}
		return role.startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
	}

	static RequestedRole mapRequestedRole(RoleName roleName) {
		return switch (roleName) {
			case ROLE_ADMIN -> RequestedRole.ROLE_ADMIN;
			case ROLE_OPERATOR -> RequestedRole.ROLE_OPERATOR;
			case ROLE_FINANCE -> RequestedRole.ROLE_FINANCE;
			case ROLE_CUSTOMER -> RequestedRole.ROLE_CUSTOMER;
		};
	}

	static Set<Role> singleRole(RoleRepository roleRepository, RoleName roleName) {
		Set<Role> roles = new HashSet<>();
		roles.add(roleRepository.findByName(roleName)
				.orElseThrow(() -> new IllegalStateException(roleName + " not found")));
		return roles;
	}
}
