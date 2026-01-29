package com.finalProjectLedZeppelin.task.dto;

import com.finalProjectLedZeppelin.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request object used to update an existing task.
 * <p>
 * Contains task fields that can be modified, including title,
 * description, status, deadline, and assignee.
 *
 * @param title       updated task title; must be non-blank and no longer than 200 characters
 * @param description updated task description; may be null and must not exceed 5000 characters
 * @param status      updated task status; may be null if status is not being changed
 * @param deadline    updated task deadline; may be null
 * @param assigneeId  identifier of the user to assign the task to; may be null
 */
public record TaskUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        TaskStatus status,
        LocalDate deadline,
        Long assigneeId
) {
}
