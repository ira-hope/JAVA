package com.example.project.config;

/**
 * Enables JPA repositories and database access for the WASAC system.
 */

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.project.repository")
@EnableTransactionManagement
public class DatabaseConfig {
}
