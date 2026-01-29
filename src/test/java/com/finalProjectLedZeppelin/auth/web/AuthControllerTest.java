package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.AuthResponse;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    AuthService authService;

    @Test
    void register_shouldReturnToken_whenValidBody() throws Exception {
        // given
        Mockito.when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("token-123"));
        // when / then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Test@Email.com", "pass12345")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("token-123"));
    }

    @Test
    void login_shouldReturnToken_whenValidBody() throws Exception {
        // given
        Mockito.when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("token-456"));
        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("user@test.com", "pass12345")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("token-456"));
    }

    @Test
    void register_shouldReturn400_whenBodyInvalid() throws Exception {
        // given
        // when / then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400_whenBodyInvalid() throws Exception {
        // given
        // when / then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}