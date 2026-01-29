package com.finalProjectLedZeppelin.task.dto;

import com.finalProjectLedZeppelin.task.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response object representing a task.
 * <p>
 * Contains full task details including assignment, status,
 * and lifecycle timestamps.
 *
 * @param id            unique identifier of the task
 * @param assigneeId    identifier of the assigned user, if any
 * @param assigneeEmail email address of the assigned user, if available
 * @param title         task title
 * @param description   task description
 * @param status        current status of the task
 * @param deadline      optional task deadline
 * @param createdAt     timestamp when the task was created
 * @param updatedAt     timestamp when the task was last updated
 */
public record TaskResponse(
        Long id,
        Long assigneeId,
        String assigneeEmail,
        String title,
        String description,
        TaskStatus status,
        LocalDate deadline,
        Instant createdAt,
        Instant updatedAt
) {
}
