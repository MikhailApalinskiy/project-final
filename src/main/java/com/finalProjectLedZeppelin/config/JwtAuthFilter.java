package com.finalProjectLedZeppelin.config;

import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter responsible for JWT-based authentication.
 * <p>
 * Extracts and validates a JWT access token from the {@code Authorization}
 * header using the {@code Bearer} scheme. If the token is valid, an
 * {@link org.springframework.security.core.Authentication} object is created
 * and stored in the {@link SecurityContextHolder}.
 */
@Log4j2
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * Authorization header prefix for bearer tokens.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * JWT claim name containing the user identifier.
     */
    private static final String CLAIM_UID = "uid";

    /**
     * JWT claim name containing the user role.
     */
    private static final String CLAIM_ROLE = "role";

    private final JwtService jwtService;

    /**
     * Creates a new {@code JwtAuthFilter} instance.
     *
     * @param jwtService service used to parse and validate JWT tokens
     */
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Performs JWT authentication for incoming HTTP requests.
     * <p>
     * If a valid JWT token is present, the security context is populated
     * with an authenticated principal containing the user ID and role.
     * Requests without a token or with an invalid token are processed
     * without authentication.
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain filter chain to continue processing
     * @throws ServletException in case of servlet errors
     * @throws IOException      in case of I/O errors
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @SuppressWarnings("NullableProblems") HttpServletResponse response,
            @SuppressWarnings("NullableProblems") FilterChain filterChain
    ) throws ServletException, IOException {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(auth) || !auth.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.debug("JWT auth attempt (path={})", request.getRequestURI());
        String token = auth.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            log.warn("JWT auth failed: empty token (path={})", request.getRequestURI());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims claims = jwtService.parse(token);
            Number uidNumber = claims.get(CLAIM_UID, Number.class);
            String role = claims.get(CLAIM_ROLE, String.class);
            if (uidNumber == null || !StringUtils.hasText(role)) {
                log.warn("JWT auth failed: missing claims (path={}, uidPresent={}, rolePresent={})",
                        request.getRequestURI(),
                        uidNumber != null,
                        StringUtils.hasText(role)
                );
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            Long userId = uidNumber.longValue();
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT auth success (path={}, userId={}, role={})", request.getRequestURI(), userId, role);
        } catch (Exception ex) {
            log.warn("JWT auth failed: invalid token (path={}, error={})",
                    request.getRequestURI(),
                    ex.getClass().getSimpleName()
            );
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
