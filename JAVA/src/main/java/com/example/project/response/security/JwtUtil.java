package com.example.project.security;

/**
 * Creates, parses, and validates JWT access and refresh tokens.
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

	private final SecretKey key;
	private final long expirationMs;
	private final long refreshExpirationMs;

	public JwtUtil(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs,
			@Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
		byte[] keyBytes = secret.length() >= 32
				? secret.getBytes()
				: Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.expirationMs = expirationMs;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public String generateAccessToken(UserDetails userDetails) {
		return buildToken(new HashMap<>(), userDetails.getUsername(), expirationMs);
	}

	public String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("type", "refresh");
		return buildToken(claims, userDetails.getUsername(), refreshExpirationMs);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public boolean isRefreshToken(String token) {
		return "refresh".equals(extractClaim(token, claims -> claims.get("type", String.class)));
	}

	public LocalDateTime extractExpirationAsLocalDateTime(String token) {
		return Instant.ofEpochMilli(extractExpiration(token).getTime())
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private <T> T extractClaim(String token, Function<Claims, T> resolver) {
		Claims claims = extractAllClaims(token);
		return resolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
		Date now = new Date();
		return Jwts.builder()
				.claims(extraClaims)
				.subject(subject)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expiration))
				.signWith(key)
				.compact();
	}
}
