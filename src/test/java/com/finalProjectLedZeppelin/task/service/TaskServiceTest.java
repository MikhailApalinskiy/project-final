package com.finalProjectLedZeppelin.task.service;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.model.Task;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.repo.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceUnitTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    TaskService taskService;

    @Test
    void create_shouldSaveWithNullAssignee_whenAssigneeIdNull() {
        // Given
        TaskCreateRequest req = new TaskCreateRequest("t", "d", LocalDate.of(2030,1,1), null);
        Task saved = task(1L, null);
        when(taskRepository.save(any(Task.class))).thenReturn(saved);
        // When
        TaskResponse res = taskService.create(req);
        // Then
        assertThat(res.id()).isEqualTo(1L);
        assertThat(res.assigneeId()).isNull();
        verify(taskRepository).save(any(Task.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void create_shouldThrow_whenAssigneeNotFound() {
        // Given
        TaskCreateRequest req = new TaskCreateRequest("t", null, null, 99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        // When / Then
        assertThatThrownBy(() -> taskService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found: 99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void get_shouldThrowAccessDenied_whenNotAdminAndNotAssignee() {
        // Given
        Task t = task(1L, user(100L, "x@test.com"));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(t));
        // When / Then
        assertThatThrownBy(() -> taskService.get(7L, false, 1L))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessage("Not your task");
    }

    @Test
    void list_shouldCallAdminRepoMethod_whenIsAdminAndHasStatusOnly() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(taskRepository.findAllByStatus(eq(TaskStatus.TODO), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(task(1L, null)), pageable, 1));
        // When
        Page<TaskResponse> page = taskService.list(7L, true, TaskStatus.TODO, null, null, pageable);
        // Then
        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(taskRepository).findAllByStatus(TaskStatus.TODO, pageable);
        verify(taskRepository, never()).findAll(pageable);
    }

    private static Task task(Long id, User assignee) {
        Task t = new Task();
        t.setId(id);
        t.setAssignee(assignee);
        t.setTitle("t");
        t.setDescription("d");
        t.setStatus(TaskStatus.TODO);
        t.setDeadline(LocalDate.of(2030,1,1));
        t.setCreatedAt(Instant.parse("2020-01-01T00:00:00Z"));
        t.setUpdatedAt(Instant.parse("2020-01-01T00:00:00Z"));
        return t;
    }

    private static User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }
}