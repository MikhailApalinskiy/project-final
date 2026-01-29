package com.finalProjectLedZeppelin.task.web;

import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.dto.TaskStatusUpdateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskUpdateRequest;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TaskController.class)
@Import(TaskControllerTest.TestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    TaskService taskService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
                    .build();
        }
    }

    private Authentication userAuth(long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private Authentication adminAuth(long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private TaskResponse sample(Long id, Long assigneeId) {
        return new TaskResponse(
                id,
                assigneeId,
                assigneeId != null ? "u@test.com" : null,
                "title",
                "desc",
                TaskStatus.TODO,
                LocalDate.parse("2026-01-10"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    @Test
    void create_shouldReturn200_whenAdmin() throws Exception {
        // given
        TaskCreateRequest req = new TaskCreateRequest(
                "title", "desc", LocalDate.parse("2026-01-10"), 1L
        );
        Mockito.when(taskService.create(any(TaskCreateRequest.class)))
                .thenReturn(sample(7L, 1L));
        // when / then
        mockMvc.perform(post("/api/tasks")
                        .with(authentication(adminAuth(999L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.assigneeId").value(1));
        Mockito.verify(taskService).create(any(TaskCreateRequest.class));
    }

    @Test
    void create_shouldReturn403_whenNotAdmin() throws Exception {
        // given
        TaskCreateRequest req = new TaskCreateRequest(
                "title", "desc", LocalDate.parse("2026-01-10"), 1L
        );
        // when / then
        mockMvc.perform(post("/api/tasks")
                        .with(authentication(userAuth(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
        Mockito.verifyNoInteractions(taskService);
    }

    @Test
    void updateStatus_shouldCallService_whenUser() throws Exception {
        // given
        TaskStatusUpdateRequest req = new TaskStatusUpdateRequest(TaskStatus.DONE);
        Mockito.when(taskService.updateStatus(10L, false, 9L, TaskStatus.DONE))
                .thenReturn(new TaskResponse(
                        9L, 10L, "u@test.com",
                        "title", "desc",
                        TaskStatus.DONE,
                        LocalDate.parse("2026-01-10"),
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-02T00:00:00Z")
                ));
        // when / then
        mockMvc.perform(patch("/api/tasks/9/status")
                        .with(authentication(userAuth(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
        Mockito.verify(taskService).updateStatus(10L, false, 9L, TaskStatus.DONE);
    }

    @Test
    void update_shouldReturn200_whenAdmin() throws Exception {
        // given
        TaskUpdateRequest req = new TaskUpdateRequest(
                "new title",
                "new desc",
                TaskStatus.IN_PROGRESS,
                LocalDate.parse("2026-02-01"),
                2L
        );
        Mockito.when(taskService.adminUpdate(eq(9L), any(TaskUpdateRequest.class)))
                .thenReturn(new TaskResponse(
                        9L, 2L, "a@test.com",
                        "new title", "new desc",
                        TaskStatus.IN_PROGRESS,
                        LocalDate.parse("2026-02-01"),
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-02T00:00:00Z")
                ));
        // when / then
        mockMvc.perform(put("/api/tasks/9")
                        .with(authentication(adminAuth(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assigneeId").value(2));
        Mockito.verify(taskService).adminUpdate(eq(9L), any(TaskUpdateRequest.class));
    }

    @Test
    void list_shouldPassFilters_andPageable() throws Exception {
        // given
        Page<TaskResponse> page = new PageImpl<>(
                List.of(sample(1L, 2L)),
                PageRequest.of(0, 20, Sort.by("id").descending()),
                1
        );
        Mockito.when(taskService.list(
                        eq(2L),
                        eq(true),
                        eq(TaskStatus.TODO),
                        eq(LocalDate.parse("2026-01-01")),
                        eq(LocalDate.parse("2026-01-31")),
                        any(Pageable.class)
                ))
                .thenReturn(page);
        // when / then
        mockMvc.perform(get("/api/tasks")
                        .with(authentication(adminAuth(2L)))
                        .param("status", "TODO")
                        .param("deadlineFrom", "2026-01-01")
                        .param("deadlineTo", "2026-01-31")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
        Mockito.verify(taskService).list(
                eq(2L),
                eq(true),
                eq(TaskStatus.TODO),
                eq(LocalDate.parse("2026-01-01")),
                eq(LocalDate.parse("2026-01-31")),
                any(Pageable.class)
        );
    }
}