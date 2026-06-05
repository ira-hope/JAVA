package com.example.project.config;

/**
 * Allows front-end apps to call the WASAC API from approved browser origins.
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	@Value("${app.cors.allowed-methods}")
	private String allowedMethods;

	@Value("${app.cors.allowed-headers}")
	private String allowedHeaders;

	@Value("${app.cors.max-age}")
	private long maxAge;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
		config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
		if ("*".equals(allowedHeaders)) {
			config.addAllowedHeader("*");
		} else {
			config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
		}
		config.setAllowCredentials(true);
		config.setMaxAge(maxAge);
		config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
