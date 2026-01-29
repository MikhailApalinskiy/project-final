package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.UpdateUserRoleRequest;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void list_shouldReturnPage_whenAdmin_andNoQuery() throws Exception {
        // given
        User u = new User();
        u.setId(1L);
        u.setEmail("admin@test.com");
        u.setRole(UserRole.ADMIN);
        u.setCreatedAt(Instant.now());
        Page<User> page = new PageImpl<>(List.of(u), PageRequest.of(0, 20), 1);
        Mockito.when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        // when / then
        mockMvc.perform(get("/api/admin/users").with(adminUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$.content[0].role").value("ADMIN"));
    }

    @Test
    void list_shouldUseSearchQuery_whenQProvided() throws Exception {
        // given
        Mockito.when(userRepository.findByEmailContainingIgnoreCase(eq("john"), any(Pageable.class)))
                .thenReturn(Page.empty());
        // when / then
        mockMvc.perform(get("/api/admin/users")
                        .with(adminUser())
                        .param("q", " john "))
                .andExpect(status().isOk());
        Mockito.verify(userRepository)
                .findByEmailContainingIgnoreCase(eq("john"), any(Pageable.class));
    }

    @Test
    void updateRole_shouldUpdateRole_whenAdmin() throws Exception {
        // given
        User u = new User();
        u.setId(5L);
        u.setEmail("user@test.com");
        u.setRole(UserRole.USER);
        u.setCreatedAt(Instant.now());
        Mockito.when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        // when / then
        mockMvc.perform(patch("/api/admin/users/5/role")
                        .with(adminUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRoleRequest("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updateRole_shouldReturn404_whenUserNotFound() throws Exception {
        // given
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
        // when / then
        mockMvc.perform(patch("/api/admin/users/99/role")
                        .with(adminUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRoleRequest("ADMIN"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldDeleteUser_whenAdmin_andNotSelf() throws Exception {
        // given
        Mockito.when(userRepository.existsById(10L)).thenReturn(true);
        // when / then
        mockMvc.perform(delete("/api/admin/users/10")
                        .with(adminAuthWithId()) // principal = Long
                        .with(csrf()))
                .andExpect(status().isOk());
        Mockito.verify(userRepository).deleteById(10L);
    }

    @Test
    void delete_shouldReturn400_whenDeletingSelf() throws Exception {
        // when / then
        mockMvc.perform(delete("/api/admin/users/1")
                        .with(adminAuthWithId())
                        .with(csrf()))
                .andExpect(status().isBadRequest());
        Mockito.verify(userRepository, Mockito.never()).deleteById(anyLong());
    }

    private static RequestPostProcessor adminUser() {
        return user("admin").roles("ADMIN");
    }

    private static RequestPostProcessor adminAuthWithId() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(
                1L,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        auth.setAuthenticated(true);
        return authentication(auth);
    }
}