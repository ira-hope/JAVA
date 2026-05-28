package com.example.project.config;

import com.example.project.entity.Role;
import com.example.project.entity.Role.RoleName;
import com.example.project.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final RoleRepository roleRepository;

	@Override
	public void run(String... args) {
		for (RoleName roleName : RoleName.values()) {
			roleRepository.findByName(roleName).orElseGet(() -> {
				log.info("Seeding role: {}", roleName);
				return roleRepository.save(Role.builder().name(roleName).build());
			});
		}
	}
}
