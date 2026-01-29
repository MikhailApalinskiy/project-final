package com.finalProjectLedZeppelin.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Service responsible for generating and parsing JSON Web Tokens (JWT).
 * <p>
 * Provides functionality for creating access tokens with user-specific claims
 * and validating/parsing existing JWTs.
 */
@Log4j2
@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenMinutes;

    /**
     * Creates a new {@code JwtService} instance.
     *
     * @param secret             secret key used for signing JWTs
     * @param accessTokenMinutes access token time-to-live in minutes
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        log.info("JWT service initialized (accessTokenTtlMinutes={})", accessTokenMinutes);
    }

    /**
     * Generates a signed JWT access token for the given user.
     * <p>
     * The token includes the user's identifier and role as custom claims
     * and uses the email as the subject.
     *
     * @param userId unique identifier of the user
     * @param email  user's email address (JWT subject)
     * @param role   user's role
     * @return signed JWT access token
     */
    public String generateAccessToken(long userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);
        log.debug("JWT access token generated (uid={}, sub={}, role={}, exp={})", userId, email, role, exp);
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Parses and validates a signed JWT access token.
     *
     * @param token JWT token to parse
     * @return extracted JWT claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired,
     *                                      or cannot be verified
     */
    public Claims parse(String token) {
        log.trace("JWT parse attempt");
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
