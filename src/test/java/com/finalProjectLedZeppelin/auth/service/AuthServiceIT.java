package com.finalProjectLedZeppelin.auth.service;

import com.finalProjectLedZeppelin.Application;
import com.finalProjectLedZeppelin.auth.dto.AuthResponse;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(classes = Application.class)
class AuthServiceIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // given (DB)
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        // given (schema management)
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.liquibase.enabled", () -> "false");
        // given (jwt props)
        r.add("app.jwt.secret", () -> "A9fQX7M@Z2eK!sR4L%Jt6D#H0xP^B8m$C3Y5NWEVwUqTGaSdFh1Okr");
        r.add("app.jwt.access-token-minutes", () -> "60");
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {
        // given
        userRepository.deleteAll();
    }

    @Test
    void register_shouldPersistUser_andReturnJwt() {
        // given
        RegisterRequest req = new RegisterRequest("  John.Doe@Example.COM  ", "pass123");
        // when
        AuthResponse resp = authService.register(req);
        // then
        assertNotNull(resp);
        assertNotNull(resp.accessToken());
        assertFalse(resp.accessToken().isBlank());
        var u = userRepository.findByEmail("john.doe@example.com").orElseThrow();
        assertNotNull(u.getId());
        assertNotNull(u.getPasswordHash());
        assertNotEquals("pass123", u.getPasswordHash());
        assertNotNull(u.getCreatedAt());
        assertNotNull(u.getRole());
    }

    @Test
    void register_shouldThrowEmailAlreadyExistsException_whenDuplicateEmail() {
        // given
        authService.register(new RegisterRequest("john@ex.com", "pass123"));
        // when / then
        assertThrows(AuthService.EmailAlreadyExistsException.class,
                () -> authService.register(new RegisterRequest("  JOHN@EX.COM  ", "pass456")));
    }

    @Test
    void login_shouldReturnJwt_whenCredentialsValid() {
        // given
        authService.register(new RegisterRequest("john@ex.com", "pass123"));
        // when
        AuthResponse resp = authService.login(new LoginRequest("  JOHN@EX.COM ", "pass123"));
        // then
        assertNotNull(resp);
        assertNotNull(resp.accessToken());
        assertFalse(resp.accessToken().isBlank());
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenPasswordInvalid() {
        // given
        authService.register(new RegisterRequest("john@ex.com", "pass123"));
        // when / then
        assertThrows(AuthService.BadCredentialsException.class,
                () -> authService.login(new LoginRequest("john@ex.com", "wrong")));
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenUserNotFound() {
        // given
        // when / then
        assertThrows(AuthService.BadCredentialsException.class,
                () -> authService.login(new LoginRequest("missing@ex.com", "pass123")));
    }
}
