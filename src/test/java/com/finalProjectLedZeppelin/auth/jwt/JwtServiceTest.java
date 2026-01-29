package com.finalProjectLedZeppelin.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET =
            "0123456789abcdef0123456789abcdef";
    private static final long ACCESS_MINUTES = 15;

    @Test
    void generateAccessToken_shouldGenerateValidToken() {
        // given
        JwtService jwtService = new JwtService(SECRET, ACCESS_MINUTES);
        long userId = 1L;
        String email = "user@test.com";
        String role = "USER";
        Instant before = Instant.now();
        // when
        String token = jwtService.generateAccessToken(userId, email, role);
        Claims claims = jwtService.parse(token);
        Instant after = Instant.now();
        // then
        assertNotNull(token);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("uid", Number.class).longValue());
        assertEquals(role, claims.get("role", String.class));
        Instant iat = claims.getIssuedAt().toInstant();
        Instant exp = claims.getExpiration().toInstant();
        assertFalse(iat.isBefore(before.minusSeconds(2)));
        assertFalse(iat.isAfter(after.plusSeconds(2)));
        assertTrue(exp.isAfter(iat));
        long deltaSeconds = Duration.between(iat, exp).getSeconds();
        long expected = Duration.ofMinutes(ACCESS_MINUTES).getSeconds();
        assertTrue(Math.abs(deltaSeconds - expected) <= 2);
    }

    @Test
    void parse_shouldThrowException_whenTokenIsTampered() {
        // given
        JwtService jwtService = new JwtService(SECRET, ACCESS_MINUTES);
        String token = jwtService.generateAccessToken(1L, "a@b.com", "USER");
        String tampered = token.substring(0, token.length() - 2) + "aa";
        // when / then
        assertThrows(JwtException.class, () -> jwtService.parse(tampered));
    }

    @Test
    void parse_shouldThrowException_whenTokenExpired() {
        // given
        JwtService jwtService = new JwtService(SECRET, -1);
        String token = jwtService.generateAccessToken(1L, "a@b.com", "USER");
        // when / then
        assertThrows(ExpiredJwtException.class, () -> jwtService.parse(token));
    }

    @Test
    void constructor_shouldThrowWeakKeyException_whenSecretTooShort() {
        // given
        String shortSecret = "short";
        // when / then
        assertThrows(WeakKeyException.class, () -> new JwtService(shortSecret, 15));
    }
}