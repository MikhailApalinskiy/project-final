package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.AuthResponse;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for authentication operations.
 * <p>
 * Exposes endpoints for user registration and login.
 */
@Log4j2
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates a new {@code AuthController} instance.
     *
     * @param authService service responsible for authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     * <p>
     * Validates the provided registration data and returns
     * an access token upon successful registration.
     *
     * @param req registration request containing user credentials
     * @return authentication response with a generated access token
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        log.info("Auth register endpoint called (email={})", req.email());
        return authService.register(req);
    }

    /**
     * Authenticates a user.
     * <p>
     * Validates user credentials and returns an access token
     * if authentication is successful.
     *
     * @param req login request containing user credentials
     * @return authentication response with a generated access token
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        log.info("Auth login endpoint called (email={})", req.email());
        return authService.login(req);
    }
}
