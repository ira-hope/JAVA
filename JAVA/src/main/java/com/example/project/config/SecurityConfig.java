package com.example.project.config;

/**
 * Sets up JWT security, role-based access, and public API routes.
 */

import com.example.project.security.CustomUserDetailsService;
import com.example.project.security.JwtAuthenticationEntryPoint;
import com.example.project.security.JwtFilter;
import com.example.project.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtFilter jwtFilter;
	private final RateLimitFilter rateLimitFilter;
	private final JwtAuthenticationEntryPoint entryPoint;
	private final CustomUserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> {})
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(
								"/api/auth/**",
								"/api/otp/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/api-docs/**",
								"/webjars/**",
								"/actuator/health",
								"/actuator/info")
						.permitAll()
						.requestMatchers("/actuator/**").hasRole("ADMIN")
						.requestMatchers("/api/users/me").authenticated()
						.requestMatchers("/api/users/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/customers").hasRole("ADMIN")
						.requestMatchers("/api/customers/me").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/meters").hasRole("ADMIN")
						.requestMatchers("/api/meters/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/api/readings/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/api/tariffs/**").hasAnyRole("ADMIN", "FINANCE", "OPERATOR")
						.requestMatchers(HttpMethod.POST, "/api/tariffs").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/tariffs/**").hasRole("ADMIN")
						.requestMatchers("/api/bills/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/bills/generate/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers(HttpMethod.PUT, "/api/bills/*/approve").hasAnyRole("ADMIN", "FINANCE")
						.requestMatchers(HttpMethod.GET, "/api/bills").hasAnyRole("ADMIN", "FINANCE")
						.requestMatchers(HttpMethod.POST, "/api/payments/**").hasAnyRole("ADMIN", "FINANCE")
						.requestMatchers(HttpMethod.GET, "/api/payments").hasAnyRole("ADMIN", "FINANCE")
						.requestMatchers("/api/payments/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/notifications").hasRole("ADMIN")
						.requestMatchers("/api/notifications/**").authenticated()
						.anyRequest().authenticated())
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
