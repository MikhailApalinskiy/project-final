package com.finalProjectLedZeppelin.task.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TaskTest {

    @Test
    void prePersist_shouldSetCreatedAtAndUpdatedAt_andDefaultStatus_whenNulls() {
        // Given
        Task task = new Task();
        task.setTitle("t");
        task.setStatus(null);
        task.setCreatedAt(null);
        task.setUpdatedAt(null);
        // When
        task.prePersist();
        // Then
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getUpdatedAt()).isNotNull();
        assertThat(task.getUpdatedAt()).isEqualTo(task.getCreatedAt());
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void prePersist_shouldNotOverrideCreatedAt_whenAlreadySet() {
        // Given
        Instant created = Instant.parse("2020-01-01T00:00:00Z");
        Task task = new Task();
        task.setTitle("t");
        task.setCreatedAt(created);
        task.setStatus(null);
        // When
        task.prePersist();
        // Then
        assertThat(task.getCreatedAt()).isEqualTo(created);
        assertThat(task.getUpdatedAt()).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void preUpdate_shouldUpdateUpdatedAt() throws Exception {
        // Given
        Task task = new Task();
        task.setTitle("t");
        task.prePersist();
        Instant before = task.getUpdatedAt();
        Thread.sleep(2);
        // When
        task.preUpdate();
        // Then
        assertThat(task.getUpdatedAt()).isAfter(before);
    }
}