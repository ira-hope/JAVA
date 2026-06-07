package com.example.project.security;

/**
 * Spring Security user object built from a WASAC User entity.
 */

import com.example.project.entity.Role;
import com.example.project.entity.User;
import com.example.project.entity.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

	private final Long id;
	private final Long customerId;
	private final String email;
	private final String password;
	private final boolean enabled;
	private final boolean accountLocked;
	private final LocalDateTime accountLockedUntil;
	private final Collection<? extends GrantedAuthority> authorities;

	public UserPrincipal(User user) {
		this.id = user.getId();
		this.customerId = user.getCustomerId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.enabled = user.getStatus() == UserStatus.ACTIVE;
		this.accountLocked = user.isAccountLocked();
		this.accountLockedUntil = user.getAccountLockedUntil();
		this.authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toSet());
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		if (!accountLocked) {
			return true;
		}
		return accountLockedUntil != null && accountLockedUntil.isBefore(LocalDateTime.now());
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public Set<String> getRoleNames() {
		return authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
	}
}
