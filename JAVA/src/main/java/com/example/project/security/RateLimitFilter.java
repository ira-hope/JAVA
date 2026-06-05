package com.example.project.security;

/**
 * Limits how many requests a client can send per minute.
 */

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

	@Value("${app.rate-limit.requests-per-minute}")
	private int requestsPerMinute;

	private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (isExcluded(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String clientKey = resolveClientKey(request);
		RateLimitBucket bucket = buckets.computeIfAbsent(clientKey, key -> new RateLimitBucket(requestsPerMinute));

		if (!bucket.tryConsume()) {
			writeRateLimitResponse(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isExcluded(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/actuator")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/api-docs")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/webjars");
	}

	private String resolveClientKey(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(
				"{\"success\":false,\"message\":\"Rate limit exceeded. Try again later.\"}");
	}

	private static final class RateLimitBucket {

		private final int maxRequests;
		private final AtomicInteger count = new AtomicInteger(0);
		private volatile long windowStart = Instant.now().getEpochSecond();

		private RateLimitBucket(int maxRequests) {
			this.maxRequests = maxRequests;
		}

		synchronized boolean tryConsume() {
			long now = Instant.now().getEpochSecond();
			if (now - windowStart >= 60) {
				windowStart = now;
				count.set(0);
			}
			return count.incrementAndGet() <= maxRequests;
		}
	}
}
