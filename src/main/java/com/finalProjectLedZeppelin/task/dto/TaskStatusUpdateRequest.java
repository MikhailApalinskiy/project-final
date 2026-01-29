package com.finalProjectLedZeppelin.task.dto;

import com.finalProjectLedZeppelin.task.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request object used to update the status of a task.
 *
 * @param status new status to be assigned to the task
 */
public record TaskStatusUpdateRequest(
        @NotNull TaskStatus status
) {
}
