package com.finalProjectLedZeppelin.config;

import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    JwtService jwtService;
    @Mock
    Claims claims;
    @Mock
    FilterChain filterChain;
    JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthentication_whenBearerTokenIsValid() throws ServletException, IOException {
        // given
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        req.addHeader("Authorization", "Bearer good.token");
        when(jwtService.parse("good.token")).thenReturn(claims);
        when(claims.get(eq("uid"), eq(Number.class))).thenReturn(7L);
        when(claims.get(eq("role"), eq(String.class))).thenReturn("ADMIN");
        // when
        filter.doFilter(req, res, filterChain);
        // then
        verify(filterChain).doFilter(req, res);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertInstanceOf(UsernamePasswordAuthenticationToken.class, auth);
        assertEquals(7L, auth.getPrincipal());
        assertEquals(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                auth.getAuthorities().stream().toList()
        );
        assertTrue(auth.isAuthenticated());
    }

    @Test
    void shouldNotSetAuthentication_whenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        // given
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        // when
        filter.doFilter(req, res, filterChain);
        // then
        verify(filterChain).doFilter(req, res);
        verifyNoInteractions(jwtService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldNotSetAuthentication_whenAuthorizationDoesNotStartWithBearer() throws ServletException, IOException {
        // given
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        req.addHeader("Authorization", "Basic abc");
        // when
        filter.doFilter(req, res, filterChain);
        // then
        verify(filterChain).doFilter(req, res);
        verifyNoInteractions(jwtService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldClearContext_whenTokenParsingFails() throws ServletException, IOException {
        // given
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        req.addHeader("Authorization", "Bearer bad.token");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
        when(jwtService.parse("bad.token")).thenThrow(new RuntimeException("boom"));
        // when
        filter.doFilter(req, res, filterChain);
        // then
        verify(filterChain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldClearContext_whenClaimsAreMissingOrInvalid() throws ServletException, IOException {
        // given
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        req.addHeader("Authorization", "Bearer token");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
        when(jwtService.parse("token")).thenReturn(claims);
        when(claims.get(eq("uid"), eq(Number.class))).thenReturn(null);
        // when
        filter.doFilter(req, res, filterChain);
        // then
        verify(filterChain).doFilter(req, res);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}