package com.finalProjectLedZeppelin.task.repo;

import com.finalProjectLedZeppelin.task.model.Task;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for managing {@link Task} entities.
 * <p>
 * Provides query methods for retrieving tasks based on assignee,
 * status, deadline ranges, and their combinations.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Retrieves a task by its identifier.
     *
     * @param id task identifier
     * @return optional containing the task if found
     */
    Optional<Task> findById(Long id);

    /**
     * Finds all tasks assigned to the specified user.
     *
     * @param assigneeId identifier of the assignee
     * @param pageable   pagination information
     * @return page of tasks assigned to the user
     */
    Page<Task> findAllByAssigneeId(Long assigneeId, Pageable pageable);

    /**
     * Finds all tasks assigned to the specified user
     * with the given status.
     *
     * @param assigneeId identifier of the assignee
     * @param status     task status
     * @param pageable   pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByAssigneeIdAndStatus(Long assigneeId, TaskStatus status, Pageable pageable);

    /**
     * Finds all tasks assigned to the specified user
     * with deadlines within the given range.
     *
     * @param assigneeId identifier of the assignee
     * @param from       start date of the deadline range (inclusive)
     * @param to         end date of the deadline range (inclusive)
     * @param pageable   pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByAssigneeIdAndDeadlineBetween(Long assigneeId, LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Finds all tasks assigned to the specified user
     * with the given status and deadline range.
     *
     * @param assigneeId identifier of the assignee
     * @param status     task status
     * @param from       start date of the deadline range (inclusive)
     * @param to         end date of the deadline range (inclusive)
     * @param pageable   pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByAssigneeIdAndStatusAndDeadlineBetween(Long assigneeId, TaskStatus status, LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Finds all unassigned tasks.
     *
     * @param pageable pagination information
     * @return page of unassigned tasks
     */
    Page<Task> findAllByAssigneeIsNull(Pageable pageable);

    /**
     * Finds all unassigned tasks with the given status.
     *
     * @param status   task status
     * @param pageable pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByAssigneeIsNullAndStatus(TaskStatus status, Pageable pageable);

    /**
     * Finds all unassigned tasks with deadlines
     * within the given range.
     *
     * @param from     start date of the deadline range (inclusive)
     * @param to       end date of the deadline range (inclusive)
     * @param pageable pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByAssigneeIsNullAndDeadlineBetween(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Finds all unassigned tasks with the given status
     * and deadline range.
     *
     * @param status   task status
     * @param from     start date of the deadline range (inclusive)
     * @param to       end date of the deadline range (inclusive)
     * @param pageable pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByAssigneeIsNullAndStatusAndDeadlineBetween(TaskStatus status, LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Finds all tasks with the given status.
     *
     * @param status   task status
     * @param pageable pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByStatus(TaskStatus status, Pageable pageable);

    /**
     * Finds all tasks with deadlines within the given range.
     *
     * @param from     start date of the deadline range (inclusive)
     * @param to       end date of the deadline range (inclusive)
     * @param pageable pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByDeadlineBetween(LocalDate from, LocalDate to, Pageable pageable);

    /**
     * Finds all tasks with the given status
     * and deadline range.
     *
     * @param status   task status
     * @param from     start date of the deadline range (inclusive)
     * @param to       end date of the deadline range (inclusive)
     * @param pageable pagination information
     * @return page of matching tasks
     */
    Page<Task> findAllByStatusAndDeadlineBetween(TaskStatus status, LocalDate from, LocalDate to, Pageable pageable);
}
