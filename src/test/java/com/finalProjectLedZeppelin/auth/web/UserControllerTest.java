package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UserRepository userRepository;

    @Test
    void search_shouldReturn403_whenNotAdmin() throws Exception {
        // given
        // when / then
        mockMvc.perform(get("/api/users").param("q", "john")
                        .with(user("u").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void search_shouldReturn200_whenAdmin() throws Exception {
        // given
        User u = new User();
        u.setId(7L);
        u.setEmail("john@test.com");
        Mockito.when(userRepository.findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc("john"))
                .thenReturn(List.of(u));
        // when / then
        mockMvc.perform(get("/api/users").param("q", " john ")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].email").value("john@test.com"));
    }

    @Test
    void search_shouldReturnEmptyList_whenQBlank() throws Exception {
        // given
        // when / then
        mockMvc.perform(get("/api/users").param("q", "   ")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        Mockito.verify(userRepository, Mockito.never())
                .findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(anyString());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testChain(HttpSecurity http) {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(b -> {
                    })
                    .build();
        }
    }
}