package com.finalProjectLedZeppelin.task.web;

import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.dto.TaskStatusUpdateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskUpdateRequest;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * REST controller providing task management endpoints.
 * <p>
 * Exposes operations for creating, retrieving, updating, deleting,
 * and listing tasks. Access rules depend on user role:
 * administrators can manage all tasks, while regular users can
 * access and modify only tasks assigned to them.
 */
@Log4j2
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new {@code TaskController} instance.
     *
     * @param taskService service responsible for task business logic
     */
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Resolves the identifier of the currently authenticated user.
     *
     * @return current user identifier
     * @throws IllegalStateException if no authenticated user is present
     */
    private static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return (Long) auth.getPrincipal();
    }

    /**
     * Creates a new task.
     * <p>
     * Accessible only to administrators.
     *
     * @param req task creation request
     * @return created task representation
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse create(@Valid @RequestBody TaskCreateRequest req) {
        log.info("Task create endpoint called");
        return taskService.create(req);
    }

    /**
     * Retrieves a task by its identifier.
     * <p>
     * Administrators can access any task. Non-admin users can access
     * only tasks assigned to them.
     *
     * @param id identifier of the task
     * @return task representation
     */
    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) {
        log.info("Task get endpoint called (taskId={})", id);
        return taskService.get(currentUserId(), isAdmin(), id);
    }

    /**
     * Updates a task as an administrator.
     * <p>
     * Allows updating task fields including title, description,
     * status, deadline, and assignee.
     *
     * @param id  identifier of the task
     * @param req update request
     * @return updated task representation
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest req) {
        log.info("Task admin update endpoint called (taskId={})", id);
        return taskService.adminUpdate(id, req);
    }

    /**
     * Updates the status of a task.
     * <p>
     * Administrators can update any task status. Regular users can
     * update the status only for tasks assigned to them.
     *
     * @param id  identifier of the task
     * @param req request containing the new task status
     * @return updated task representation
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest req) {
        log.info(
                "Task status update endpoint called (taskId={}, newStatus={})",
                id,
                req.status()
        );
        return taskService.updateStatus(currentUserId(), isAdmin(), id, req.status());
    }

    /**
     * Deletes a task.
     * <p>
     * Accessible only to administrators.
     *
     * @param id identifier of the task to delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        log.info("Task delete endpoint called (taskId={})", id);
        taskService.delete(id);
    }

    /**
     * Returns a paginated list of tasks.
     * <p>
     * Supports optional filtering by status and deadline range.
     * Administrators receive tasks across the system, while
     * regular users receive only tasks assigned to them.
     *
     * @param status       optional task status filter
     * @param deadlineFrom optional deadline range start (inclusive)
     * @param deadlineTo   optional deadline range end (inclusive)
     * @param pageable     pagination and sorting information
     * @return page of matching tasks
     */
    @GetMapping
    public Page<TaskResponse> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
            Pageable pageable
    ) {
        log.info(
                "Task list endpoint called (status={}, deadlineFrom={}, deadlineTo={}, page={}, size={})",
                status,
                deadlineFrom,
                deadlineTo,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        return taskService.list(currentUserId(), isAdmin(), status, deadlineFrom, deadlineTo, pageable);
    }

    /**
     * Determines whether the currently authenticated user
     * has administrator privileges.
     *
     * @return {@code true} if the user has the {@code ADMIN} role,
     * {@code false} otherwise
     */
    private static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}
