package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Test
    void list_shouldBeOk_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn400_whenDeletingSelf() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                        .with(adminAuthWithId(1L))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldBeOk_whenAdminDeletesOtherUser() throws Exception {
        // given
        User target = new User();
        target.setEmail("target@test.com");
        target.setPasswordHash("hash");
        target.setRole(UserRole.USER);
        target.setCreatedAt(Instant.now());
        target = userRepository.save(target);
        // when/then
        mockMvc.perform(delete("/api/admin/users/" + target.getId())
                        .with(adminAuthWithId(999L))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor adminAuthWithId(Long id) {
        var auth = new TestingAuthenticationToken(
                id,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        auth.setAuthenticated(true);
        return authentication(auth);
    }
}