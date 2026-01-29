package com.finalProjectLedZeppelin.task.model;

import com.finalProjectLedZeppelin.auth.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity representing a task.
 * <p>
 * Stores task details including assignment, status, deadline,
 * and lifecycle timestamps.
 */
@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "ix_tasks_assignee_status", columnList = "assignee_id,status"),
                @Index(name = "ix_tasks_assignee_deadline", columnList = "assignee_id,deadline")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Task {

    /**
     * Unique identifier of the task.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User assigned to the task.
     * <p>
     * May be {@code null} if the task is unassigned.
     */
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /**
     * Task title.
     * <p>
     * Must not exceed 200 characters.
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Optional task description.
     * <p>
     * Must not exceed 5000 characters.
     */
    @Column(length = 5000)
    private String description;

    /**
     * Current status of the task.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    /**
     * Optional task deadline.
     */
    private LocalDate deadline;

    /**
     * Timestamp when the task was created.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Timestamp when the task was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Initializes default values before the entity is persisted.
     * <p>
     * Sets creation and update timestamps and assigns the default
     * task status if not provided.
     */
    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = TaskStatus.TODO;
    }

    /**
     * Updates the modification timestamp before the entity is updated.
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}