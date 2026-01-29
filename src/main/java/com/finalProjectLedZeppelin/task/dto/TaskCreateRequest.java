package com.finalProjectLedZeppelin.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request object used to create a new task.
 * <p>
 * Contains task details such as title, description, optional deadline,
 * and an optional assignee.
 *
 * @param title       task title; must be non-blank and no longer than 200 characters
 * @param description optional task description; may be null and must not exceed 5000 characters
 * @param deadline    optional task deadline
 * @param assigneeId  optional identifier of the user assigned to the task
 */
public record TaskCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        LocalDate deadline,
        Long assigneeId
) {
}