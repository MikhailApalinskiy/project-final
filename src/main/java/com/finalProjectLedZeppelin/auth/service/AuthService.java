package com.finalProjectLedZeppelin.auth.service;

import com.finalProjectLedZeppelin.auth.dto.AuthResponse;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for user authentication and registration.
 * <p>
 * Handles user login, registration, password validation,
 * and access token generation.
 */
@Log4j2
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Creates a new {@code AuthService} instance.
     *
     * @param userRepository  repository used to access user data
     * @param passwordEncoder encoder used for hashing and validating passwords
     * @param jwtService      service responsible for JWT generation and parsing
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user and issues an access token.
     * <p>
     * The email is normalized before persistence. If a user with the same
     * email already exists, the registration is rejected.
     *
     * @param req registration request containing user credentials
     * @return authentication response with a generated access token
     * @throws EmailAlreadyExistsException if a user with the given email already exists
     */
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().toLowerCase().trim();
        log.info("Auth register attempt (email={})", email);
        if (userRepository.existsByEmail(email)) {
            log.warn("Auth register rejected: email already exists (email={})", email);
            throw new EmailAlreadyExistsException(email);
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(UserRole.USER);
        u = userRepository.save(u);
        log.info("Auth register success (userId={}, email={}, role={})", u.getId(), u.getEmail(), u.getRole());
        String token = jwtService.generateAccessToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token);
    }

    /**
     * Authenticates a user and issues a new access token.
     * <p>
     * Validates user credentials and returns a signed JWT
     * if authentication is successful.
     *
     * @param req login request containing user credentials
     * @return authentication response with a generated access token
     * @throws BadCredentialsException if the user does not exist
     *                                 or the password is invalid
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().toLowerCase().trim();
        log.info("Auth login attempt (email={})", email);
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Auth login failed: user not found (email={})", email);
                    return new BadCredentialsException();
                });

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            log.warn("Auth login failed: bad credentials (userId={}, email={})", u.getId(), email);
            throw new BadCredentialsException();
        }
        log.info("Auth login success (userId={}, email={}, role={})", u.getId(), u.getEmail(), u.getRole());
        String token = jwtService.generateAccessToken(u.getId(), u.getEmail(), u.getRole().name());
        return new AuthResponse(token);
    }

    /**
     * Exception thrown when attempting to register a user
     * with an email address that already exists.
     */
    public static class EmailAlreadyExistsException extends RuntimeException {

        /**
         * Creates a new exception instance.
         *
         * @param email email address that already exists
         */
        public EmailAlreadyExistsException(String email) {
            super("Email already exists: " + email);
        }
    }

    /**
     * Exception thrown when authentication fails due to
     * invalid credentials.
     */
    public static class BadCredentialsException extends RuntimeException {

        /**
         * Creates a new exception instance.
         */
        public BadCredentialsException() {
            super("Invalid email or password");
        }
    }
}
