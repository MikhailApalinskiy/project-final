package com.finalProjectLedZeppelin.task.web;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskStatusUpdateRequest;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.repo.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TaskRepository taskRepository;

    private static final AtomicInteger SEQ = new AtomicInteger();

    @BeforeEach
    void cleanDb() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User persistUser(UserRole role) {
        int n = SEQ.incrementAndGet();
        User u = new User();
        u.setEmail("user" + n + "@test.com");
        u.setPasswordHash("test-hash");
        u.setRole(role);
        return userRepository.save(u);
    }

    private UsernamePasswordAuthenticationToken auth(User u) {
        return new UsernamePasswordAuthenticationToken(
                u.getId(),
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
    }

    @Test
    void create_thenGet_shouldWork_forAdminAndUser() throws Exception {
        // Given
        User admin = persistUser(UserRole.ADMIN);
        User user = persistUser(UserRole.USER);
        TaskCreateRequest createReq = new TaskCreateRequest(
                "title",
                "desc",
                LocalDate.parse("2026-01-10"),
                user.getId()
        );
        // When
        String response = mockMvc.perform(post("/api/tasks")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(user.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long taskId = objectMapper.readTree(response).get("id").asLong();
        // Then
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .with(authentication(auth(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.assigneeId").value(user.getId()));
    }

    @Test
    void updateStatus_shouldWork_forAssigneeUser() throws Exception {
        // Given
        User admin = persistUser(UserRole.ADMIN);
        User user = persistUser(UserRole.USER);
        TaskCreateRequest createReq = new TaskCreateRequest(
                "task",
                null,
                null,
                user.getId()
        );
        String response = mockMvc.perform(post("/api/tasks")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long taskId = objectMapper.readTree(response).get("id").asLong();
        TaskStatusUpdateRequest statusReq =
                new TaskStatusUpdateRequest(TaskStatus.DONE);
        // When
        mockMvc.perform(patch("/api/tasks/{id}/status", taskId)
                        .with(authentication(auth(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
        // Then
        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .with(authentication(auth(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void list_shouldReturnOnlyUserTasks_forUser() throws Exception {
        // Given
        User admin = persistUser(UserRole.ADMIN);
        User u1 = persistUser(UserRole.USER);
        User u2 = persistUser(UserRole.USER);
        TaskCreateRequest r1 = new TaskCreateRequest("t1", null, null, u1.getId());
        TaskCreateRequest r2 = new TaskCreateRequest("t2", null, null, u2.getId());
        mockMvc.perform(post("/api/tasks")
                .with(authentication(auth(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r1)));
        mockMvc.perform(post("/api/tasks")
                .with(authentication(auth(admin)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(r2)));
        // When / Then
        mockMvc.perform(get("/api/tasks")
                        .with(authentication(auth(u1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("t1"));
    }
}
