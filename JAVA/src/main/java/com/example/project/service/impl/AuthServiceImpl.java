package com.example.project.service.impl;

import com.example.project.dto.request.LoginRequest;
import com.example.project.dto.request.RefreshTokenRequest;
import com.example.project.dto.request.RegisterRequest;
import com.example.project.dto.response.AuthResponse;
import com.example.project.dto.response.UserResponse;
import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.entity.User;
import com.example.project.exception.DuplicateResourceException;
import com.example.project.exception.ForbiddenException;
import com.example.project.exception.UnauthorizedException;
import com.example.project.mapper.UserMapper;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtil;
import com.example.project.security.UserPrincipal;
import com.example.project.service.AuthService;
import com.example.project.service.OTPService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserMapper userMapper;
	private final OTPService otpService;

	@Override
	@Transactional
	public void register(RegisterRequest request) {
		User existing = userRepository.findByEmailAndDeletedFalse(request.getEmail()).orElse(null);
		if (existing != null) {
			if (!existing.isEmailVerified()) {
				if (request.getRoles() != null) {
					existing.setRoles(resolveRoles(request.getRoles()));
					userRepository.save(existing);
				}
				com.example.project.dto.request.OtpRequest otpRequest = new com.example.project.dto.request.OtpRequest();
				otpRequest.setEmail(request.getEmail());
				otpService.generateAndSend(otpRequest);
				return;
			}
			throw new DuplicateResourceException("Email already registered");
		}

		User user = User.builder()
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.roles(resolveRoles(request.getRoles()))
				.build();

		user = userRepository.save(user);
		log.info("User registered: {}", user.getEmail());
		com.example.project.dto.request.OtpRequest otpRequest = new com.example.project.dto.request.OtpRequest();
		otpRequest.setEmail(user.getEmail());
		otpService.generateAndSend(otpRequest);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
				.orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
		if (!user.isEmailVerified()) {
			throw new ForbiddenException("Email not verified. Verify OTP before login.");
		}
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		log.info("User logged in: {}", principal.getEmail());
		return buildAuthResponse(principal);
	}

	@Override
	public AuthResponse refresh(RefreshTokenRequest request) {
		String token = request.getRefreshToken();
		if (!jwtUtil.isRefreshToken(token)) {
			throw new UnauthorizedException("Invalid refresh token");
		}
		String email = jwtUtil.extractUsername(token);
		User user = userRepository.findByEmailAndDeletedFalse(email)
				.orElseThrow(() -> new UnauthorizedException("User not found"));
		UserPrincipal principal = new UserPrincipal(user);
		if (!jwtUtil.isTokenValid(token, principal)) {
			throw new UnauthorizedException("Refresh token expired");
		}
		return buildAuthResponse(principal);
	}

	private AuthResponse buildAuthResponse(UserDetails userDetails) {
		User user = userRepository.findByEmailAndDeletedFalse(userDetails.getUsername())
				.orElseThrow(() -> new UnauthorizedException("User not found"));
		UserResponse userResponse = userMapper.toResponse(user);
		return AuthResponse.builder()
				.accessToken(jwtUtil.generateAccessToken(userDetails))
				.refreshToken(jwtUtil.generateRefreshToken(userDetails))
				.tokenType("Bearer")
				.user(userResponse)
				.build();
	}

	private Set<Role> resolveRoles(Set<String> roleNames) {
		if (roleNames == null || roleNames.isEmpty()) {
			return Set.of(roleRepository.findByName(RoleName.ROLE_USER)
					.orElseThrow(() -> new IllegalStateException("ROLE_USER not found")));
		}
		Set<Role> roles = new HashSet<>();
		for (String roleName : roleNames) {
			RoleName name = RoleName.valueOf(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
			roles.add(roleRepository.findByName(name)
					.orElseThrow(() -> new IllegalStateException("Role not found: " + roleName)));
		}
		return roles;
	}
}
