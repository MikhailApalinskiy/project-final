package com.finalProjectLedZeppelin.task.service;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.repo.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@DirtiesContext
class TaskServiceIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TaskService taskService;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_shouldPersistAndReturnResponse() {
        // Given
        User u = persistUser("u@test.com");
        u.setEmail("a@test.com");
        u = userRepository.save(u);
        TaskCreateRequest req = new TaskCreateRequest(
                "hello",
                "desc",
                LocalDate.of(2030, 1, 1),
                u.getId()
        );
        // When
        TaskResponse res = taskService.create(req);
        // Then
        assertThat(res.id()).isNotNull();
        assertThat(res.assigneeId()).isEqualTo(u.getId());
        assertThat(taskRepository.findById(res.id())).isPresent();
    }

    @Test
    void list_shouldUseDatabaseFilters() {
        // Given
        User u = userRepository.save(persistUser("u@test.com"));
        TaskCreateRequest r1 = new TaskCreateRequest("t1", null, LocalDate.of(2030,1,1), u.getId());
        TaskCreateRequest r2 = new TaskCreateRequest("t2", null, LocalDate.of(2030,2,1), u.getId());
        taskService.create(r1);
        taskService.create(r2);
        // When
        var page = taskService.list(
                u.getId(),
                false,
                null,
                LocalDate.of(2030,1,1),
                LocalDate.of(2030,1,31),
                PageRequest.of(0, 20)
        );
        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).title()).isEqualTo("t1");
    }

    private User persistUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("{noop}test");
        u.setRole(UserRole.USER);
        return userRepository.save(u);
    }
}
