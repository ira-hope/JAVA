package com.example.project.specification;

/**
 * Builds dynamic JPA filters for searching users.
 */

import com.example.project.entity.Role;
import com.example.project.entity.User;
import com.example.project.entity.enums.UserStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecification {

	private UserSpecification() {
	}

	public static Specification<User> withFilters(
			String search,
			String email,
			String fullName,
			UserStatus status,
			String role) {
		return Specification.allOf(
				searchContains(search),
				emailContains(email),
				fullNameContains(fullName),
				statusEquals(status),
				hasRole(role));
	}

	private static Specification<User> searchContains(String search) {
		if (!StringUtils.hasText(search)) {
			return null;
		}
		String pattern = "%" + search.toLowerCase() + "%";
		return (root, query, cb) -> cb.or(
				cb.like(cb.lower(root.get("email")), pattern),
				cb.like(cb.lower(root.get("fullName")), pattern),
				cb.like(cb.lower(root.get("phone")), pattern));
	}

	private static Specification<User> emailContains(String email) {
		if (!StringUtils.hasText(email)) {
			return null;
		}
		return (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
	}

	private static Specification<User> fullNameContains(String fullName) {
		if (!StringUtils.hasText(fullName)) {
			return null;
		}
		return (root, query, cb) -> cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
	}

	private static Specification<User> statusEquals(UserStatus status) {
		if (status == null) {
			return null;
		}
		return (root, query, cb) -> cb.equal(root.get("status"), status);
	}

	private static Specification<User> hasRole(String role) {
		if (!StringUtils.hasText(role)) {
			return null;
		}
		String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		Role.RoleName roleEnum = Role.RoleName.valueOf(roleName.toUpperCase());
		return (root, query, cb) -> {
			Subquery<Long> subquery = query.subquery(Long.class);
			var subRoot = subquery.from(User.class);
			Join<Object, Object> roleJoin = subRoot.join("roles");
			subquery.select(subRoot.get("id"));
			subquery.where(
					cb.equal(subRoot.get("id"), root.get("id")),
					cb.equal(roleJoin.get("name"), roleEnum));
			return root.get("id").in(subquery);
		};
	}
}
