package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.RegisterRequest;
import com.finalProjectLedZeppelin.auth.jwt.JwtService;
import com.finalProjectLedZeppelin.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    JwtService jwtService;

    @Test
    void register_shouldReturn200_andToken() throws Exception {
        // given
        Mockito.when(jwtService.generateAccessToken(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("it-token");
        // when / then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("new@test.com", "pass12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("it-token"));
    }

    @Test
    void login_shouldReturn200_andToken_whenCorrectPassword() throws Exception {
        // given
        Mockito.when(jwtService.generateAccessToken(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("it-token");
        // when
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("user@test.com", "pass12345"))))
                .andExpect(status().isOk());
        // then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@test.com", "pass12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("it-token"));
    }

    @Test
    void register_shouldReturn400_whenInvalidBody() throws Exception {
        // given
        // when / then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
