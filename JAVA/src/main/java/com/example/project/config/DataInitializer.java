package com.example.project.config;

/**
 * Seeds default WASAC roles and the admin user on first application startup.
 */

import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.entity.User;
import com.example.project.entity.enums.RequestedRole;
import com.example.project.entity.enums.UserStatus;
import com.example.project.repository.RoleRepository;
import com.example.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		for (RoleName roleName : RoleName.values()) {
			roleRepository.findByName(roleName).orElseGet(() -> {
				log.info("Seeding role: {}", roleName);
				return roleRepository.save(Role.builder().name(roleName).build());
			});
		}
		seedAdminUser();
	}

	private void seedAdminUser() {
		Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
				.orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

		userRepository.findByEmailIgnoreCase("admin@wasac.com").ifPresentOrElse(
				existing -> correctAdminUser(existing, adminRole),
				() -> createAdminUser(adminRole));
	}

	private void createAdminUser(Role adminRole) {
		User admin = User.builder()
				.fullName("WASAC Administrator")
				.email("admin@wasac.com")
				.phone("+250780000000")
				.password(passwordEncoder.encode("admin123"))
				.status(UserStatus.ACTIVE)
				.emailVerified(true)
				.requestedRole(RequestedRole.ROLE_ADMIN)
				.adminApproved(true)
				.roles(new HashSet<>(Set.of(adminRole)))
				.build();
		userRepository.save(admin);
		log.info("Seeded default admin: admin@wasac.com / admin123");
	}

	private void correctAdminUser(User admin, Role adminRole) {
		boolean updated = false;
		if (admin.getRequestedRole() != RequestedRole.ROLE_ADMIN) {
			admin.setRequestedRole(RequestedRole.ROLE_ADMIN);
			updated = true;
		}
		if (admin.getRoles().stream().noneMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
			admin.setRoles(new HashSet<>(Set.of(adminRole)));
			updated = true;
		}
		if (admin.getCustomerId() != null) {
			admin.setCustomerId(null);
			updated = true;
		}
		if (updated) {
			userRepository.save(admin);
			log.info("Corrected default admin account: requestedRole=ROLE_ADMIN, roles=[ROLE_ADMIN]");
		}
	}
}
