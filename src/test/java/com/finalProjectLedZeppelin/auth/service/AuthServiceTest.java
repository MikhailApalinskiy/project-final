package com.finalProjectLedZeppelin.auth.service;

import com.finalProjectLedZeppelin.auth.dto.AuthResponse;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldNormalizeEmail_saveUser_andReturnToken() {
        // given
        RegisterRequest req = new RegisterRequest("  John.Doe@Example.COM  ", "pass123");
        String normalizedEmail = "john.doe@example.com";
        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0, User.class);
            u.setId(10L);
            return u;
        });
        when(jwtService.generateAccessToken(10L, normalizedEmail, "USER")).thenReturn("JWT_TOKEN");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // when
        AuthResponse resp = authService.register(req);
        // then
        assertNotNull(resp);
        assertEquals("JWT_TOKEN", resp.accessToken());
        verify(userRepository).existsByEmail(normalizedEmail);
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(normalizedEmail, savedUser.getEmail());
        assertEquals("HASH", savedUser.getPasswordHash());
        assertEquals(UserRole.USER, savedUser.getRole());
        verify(jwtService).generateAccessToken(10L, normalizedEmail, "USER");
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyTaken() {
        // given
        RegisterRequest req = new RegisterRequest("  John.Doe@Example.COM  ", "pass123");
        String normalizedEmail = "john.doe@example.com";
        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(true);
        // when
        AuthService.EmailAlreadyExistsException ex =
                assertThrows(AuthService.EmailAlreadyExistsException.class, () -> authService.register(req));
        // then
        assertTrue(ex.getMessage().contains(normalizedEmail));
        verify(userRepository).existsByEmail(normalizedEmail);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_shouldNormalizeEmail_validatePassword_andReturnToken() {
        // given
        LoginRequest req = new LoginRequest("  John.Doe@Example.COM  ", "pass123");
        String normalizedEmail = "john.doe@example.com";
        User user = new User();
        user.setId(42L);
        user.setEmail(normalizedEmail);
        user.setPasswordHash("HASH");
        user.setRole(UserRole.USER);
        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "HASH")).thenReturn(true);
        when(jwtService.generateAccessToken(42L, normalizedEmail, "USER")).thenReturn("JWT_TOKEN");
        // when
        AuthResponse resp = authService.login(req);
        // then
        assertNotNull(resp);
        assertEquals("JWT_TOKEN", resp.accessToken());
        verify(userRepository).findByEmail(normalizedEmail);
        verify(passwordEncoder).matches("pass123", "HASH");
        verify(jwtService).generateAccessToken(42L, normalizedEmail, "USER");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenUserNotFound() {
        // given
        LoginRequest req = new LoginRequest("  missing@Example.COM  ", "pass123");
        String normalizedEmail = "missing@example.com";
        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.empty());
        // when
        AuthService.BadCredentialsException ex =
                assertThrows(AuthService.BadCredentialsException.class, () -> authService.login(req));
        // then
        assertEquals("Invalid email or password", ex.getMessage());
        verify(userRepository).findByEmail(normalizedEmail);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenPasswordMismatch() {
        // given
        LoginRequest req = new LoginRequest("  John.Doe@Example.COM  ", "wrong");
        String normalizedEmail = "john.doe@example.com";
        User user = new User();
        user.setId(42L);
        user.setEmail(normalizedEmail);
        user.setPasswordHash("HASH");
        user.setRole(UserRole.USER);
        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);
        // when
        AuthService.BadCredentialsException ex =
                assertThrows(AuthService.BadCredentialsException.class, () -> authService.login(req));
        // then
        assertEquals("Invalid email or password", ex.getMessage());
        verify(userRepository).findByEmail(normalizedEmail);
        verify(passwordEncoder).matches("wrong", "HASH");
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }
}