package com.finalProjectLedZeppelin.config;

import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import com.finalProjectLedZeppelin.common.error.ApiError;
import com.finalProjectLedZeppelin.common.logging.RequestIdMdcFilter;
import com.finalProjectLedZeppelin.common.logging.UserIdMdcFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security configuration for the application.
 * <p>
 * Configures stateless security using JWT authentication for API endpoints
 * and HTTP Basic authentication for actuator endpoints. Also registers
 * infrastructure filters for request and user log correlation.
 */
@Log4j2
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configures the password encoder used for hashing user passwords.
     *
     * @return password encoder implementation
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("PasswordEncoder configured: BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    /**
     * Filter responsible for assigning a request identifier
     * to each incoming HTTP request.
     *
     * @return request ID MDC filter
     */
    @Bean
    public RequestIdMdcFilter requestIdMdcFilter() {
        log.info("Security filter configured: RequestIdMdcFilter");
        return new RequestIdMdcFilter();
    }

    /**
     * Filter responsible for enriching logs with the authenticated
     * user's identifier.
     *
     * @return user ID MDC filter
     */
    @Bean
    public UserIdMdcFilter userIdMdcFilter() {
        log.info("Security filter configured: UserIdMdcFilter");
        return new UserIdMdcFilter();
    }

    /**
     * Security filter chain for actuator endpoints.
     * <p>
     * Uses HTTP Basic authentication and restricts access to monitoring
     * endpoints based on the {@code MONITORING} role.
     *
     * @param http         HTTP security configuration
     * @param objectMapper object mapper used for error responses
     * @return configured security filter chain
     */
    @Bean
    @Order(1)
    SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http, ObjectMapper objectMapper) {
        return http
                .securityMatcher("/actuator/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").hasRole("MONITORING")
                        .requestMatchers("/actuator/**").hasRole("MONITORING")
                        .anyRequest().denyAll()
                )
                .httpBasic(withDefaults())
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, ex) ->
                                writeApiError(response, objectMapper, HttpStatus.UNAUTHORIZED, "Unauthorized", request.getRequestURI())
                        )
                        .accessDeniedHandler((request, response, ex) ->
                                writeApiError(response, objectMapper, HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI())
                        )
                )
                .build();
    }

    /**
     * Security filter chain for API endpoints.
     * <p>
     * Applies JWT-based authentication, disables sessions, and allows
     * unauthenticated access only to public authentication endpoints.
     *
     * @param http               HTTP security configuration
     * @param jwtService         service used to validate JWT tokens
     * @param objectMapper       object mapper used for error responses
     * @param requestIdMdcFilter filter for request ID correlation
     * @param userIdMdcFilter    filter for user ID log correlation
     * @return configured security filter chain
     */
    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            ObjectMapper objectMapper,
            RequestIdMdcFilter requestIdMdcFilter,
            UserIdMdcFilter userIdMdcFilter
    ) {
        log.info("SecurityFilterChain building (stateless=true, publicPaths=[/api/auth/**, /actuator/health])");
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtService);
        log.debug("Security filters order: RequestIdMdcFilter -> JwtAuthFilter -> UserIdMdcFilter");
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) ->
                                writeApiError(response, objectMapper, HttpStatus.UNAUTHORIZED, "Unauthorized", request.getRequestURI())
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeApiError(response, objectMapper, HttpStatus.FORBIDDEN, "Access denied", request.getRequestURI())
                        )
                )
                .addFilterBefore(requestIdMdcFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(userIdMdcFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Writes a standardized {@link ApiError} response directly
     * to the HTTP response output stream.
     *
     * @param response     HTTP response
     * @param objectMapper object mapper for JSON serialization
     * @param status       HTTP status to return
     * @param message      error message
     * @param path         request path
     * @throws IOException in case of I/O errors
     */
    private static void writeApiError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
        response.flushBuffer();
    }
}
