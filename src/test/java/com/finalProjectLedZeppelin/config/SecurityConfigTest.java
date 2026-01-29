package com.finalProjectLedZeppelin.config;

import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        SecurityConfigTest.PublicAuthController.class,
        SecurityConfigTest.ProtectedController.class
})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    JwtService jwtService;

    @TestConfiguration
    static class TestControllersConfig {

        @Bean
        PublicAuthController publicAuthController() {
            return new PublicAuthController();
        }

        @Bean
        ProtectedController protectedController() {
            return new ProtectedController();
        }
    }

    @RestController
    @RequestMapping("/api/auth")
    static class PublicAuthController {
        @GetMapping("/ping")
        String ping() {
            return "ok";
        }
    }

    @RestController
    @RequestMapping("/api")
    static class ProtectedController {

        @GetMapping("/me")
        String me(org.springframework.security.core.Authentication auth) {
            return String.valueOf(auth.getPrincipal());
        }

        @GetMapping("/admin-only")
        @PreAuthorize("hasRole('ADMIN')")
        String adminOnly() {
            return "secret";
        }
    }

    @Test
    void authEndpoints_shouldBePermitAll() throws Exception {
        // given
        // when / then
        mockMvc.perform(get("/api/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void otherEndpoints_shouldReturn401_whenNoToken() throws Exception {
        // given
        // when / then
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/me"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void protectedEndpoint_shouldReturn200_whenValidBearerToken() throws Exception {
        // given
        Claims claims = Mockito.mock(Claims.class);
        when(jwtService.parse("good.token")).thenReturn(claims);
        when(claims.get(eq("uid"), eq(Number.class))).thenReturn(7L);
        when(claims.get(eq("role"), eq(String.class))).thenReturn("USER");
        // when / then
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer good.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void adminOnly_shouldReturn403_whenRoleIsNotAdmin() throws Exception {
        // given
        Claims claims = Mockito.mock(Claims.class);
        when(jwtService.parse("user.token")).thenReturn(claims);
        when(claims.get(eq("uid"), eq(Number.class))).thenReturn(10L);
        when(claims.get(eq("role"), eq(String.class))).thenReturn("USER");
        // when / then
        mockMvc.perform(get("/api/admin-only")
                        .header("Authorization", "Bearer user.token"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.path").value("/api/admin-only"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void adminOnly_shouldReturn200_whenRoleIsAdmin() throws Exception {
        // given
        Claims claims = Mockito.mock(Claims.class);
        when(jwtService.parse("admin.token")).thenReturn(claims);
        when(claims.get(eq("uid"), eq(Number.class))).thenReturn(1L);
        when(claims.get(eq("role"), eq(String.class))).thenReturn("ADMIN");
        // when / then
        mockMvc.perform(get("/api/admin-only")
                        .header("Authorization", "Bearer admin.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("secret"));
    }

    @Test
    void shouldReturn401_whenBearerTokenIsInvalid() throws Exception {
        // given
        when(jwtService.parse("bad.token")).thenThrow(new RuntimeException("invalid"));
        // when / then
        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer bad.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }
}