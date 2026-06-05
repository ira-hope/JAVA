package com.example.project.config;

/**
 * Applies PostgreSQL schema fixes as soon as the DataSource is ready — before JPA/Hibernate
 * runs queries or ddl-auto updates (e.g. password_set column, ROLE_ADMIN check constraint).
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DatabaseSchemaFixer implements BeanPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaFixer.class);
	private static final AtomicBoolean MIGRATED = new AtomicBoolean(false);

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof DataSource && MIGRATED.compareAndSet(false, true)) {
			applyFixes(new JdbcTemplate((DataSource) bean));
		}
		return bean;
	}

	private void applyFixes(JdbcTemplate jdbc) {
		if (!usersTableExists(jdbc)) {
			return;
		}
		ensurePasswordSetColumn(jdbc);
		updateRequestedRoleCheckConstraint(jdbc);
	}

	private boolean usersTableExists(JdbcTemplate jdbc) {
		Boolean exists = jdbc.queryForObject(
				"""
				SELECT EXISTS (
					SELECT 1
					FROM information_schema.tables
					WHERE table_schema = 'public' AND table_name = 'users'
				)
				""",
				Boolean.class);
		return Boolean.TRUE.equals(exists);
	}

	private void ensurePasswordSetColumn(JdbcTemplate jdbc) {
		try {
			Boolean columnExists = jdbc.queryForObject(
					"""
					SELECT EXISTS (
						SELECT 1
						FROM information_schema.columns
						WHERE table_schema = 'public'
						  AND table_name = 'users'
						  AND column_name = 'password_set'
					)
					""",
					Boolean.class);

			if (!Boolean.TRUE.equals(columnExists)) {
				jdbc.execute("ALTER TABLE users ADD COLUMN password_set boolean DEFAULT true");
				log.info("Added users.password_set column");
			}

			jdbc.execute("UPDATE users SET password_set = true WHERE password_set IS NULL");
			jdbc.execute("ALTER TABLE users ALTER COLUMN password_set SET DEFAULT true");
			jdbc.execute("ALTER TABLE users ALTER COLUMN password_set SET NOT NULL");
			log.info("Ensured users.password_set is populated and NOT NULL");
		} catch (Exception ex) {
			log.warn("Could not migrate users.password_set column: {}", ex.getMessage());
		}
	}

	private void updateRequestedRoleCheckConstraint(JdbcTemplate jdbc) {
		try {
			jdbc.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_requested_role_check");
			jdbc.execute("""
					ALTER TABLE users ADD CONSTRAINT users_requested_role_check
					CHECK (requested_role IN (
						'ROLE_ADMIN',
						'ROLE_CUSTOMER',
						'ROLE_OPERATOR',
						'ROLE_FINANCE'
					))
					""");
			log.info("Updated users.requested_role check constraint (ROLE_ADMIN supported)");
		} catch (Exception ex) {
			log.warn("Could not update users_requested_role_check constraint: {}", ex.getMessage());
		}
	}
}
