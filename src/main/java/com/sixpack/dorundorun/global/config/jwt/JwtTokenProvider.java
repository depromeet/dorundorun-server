package com.sixpack.dorundorun.global.config.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	private static final String CLAIM_USER_ID = "userId";
	private static final String CLAIM_NICKNAME = "nickname";
	private static final String TOKEN_TYPE_ACCESS = "access";
	private static final String TOKEN_TYPE_REFRESH = "refresh";

	public String generateAccessToken(User user) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtProperties.accessTokenValidity());

		return Jwts.builder()
			.subject(user.getId().toString())
			.claim(CLAIM_USER_ID, user.getId())
			.claim(CLAIM_NICKNAME, user.getNickname())
			.claim("type", TOKEN_TYPE_ACCESS)
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	public String generateRefreshToken(User user) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtProperties.refreshTokenValidity());

		return Jwts.builder()
			.subject(user.getId().toString())
			.claim(CLAIM_USER_ID, user.getId())
			.claim("type", TOKEN_TYPE_REFRESH)
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSigningKey())
			.compact();
	}

	public boolean validate(String token) {
		try {
			Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (SignatureException e) {
			log.warn("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.warn("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.warn("Expired JWT token: {}", e.getMessage());
			throw AuthErrorCode.EXPIRED_TOKEN.format();
		} catch (UnsupportedJwtException e) {
			log.warn("Unsupported JWT token: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}

	public Long getUserId(String token) {
		Claims claims = getClaims(token);
		return claims.get(CLAIM_USER_ID, Long.class);
	}

	public String getNickname(String token) {
		Claims claims = getClaims(token);
		return claims.get(CLAIM_NICKNAME, String.class);
	}

	public Date getExpiration(String token) {
		Claims claims = getClaims(token);
		return claims.getExpiration();
	}

	public boolean isExpired(String token) {
		try {
			Date expiration = getExpiration(token);
			return expiration.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	public String getTokenType(String token) {
		Claims claims = getClaims(token);
		return claims.get("type", String.class);
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
