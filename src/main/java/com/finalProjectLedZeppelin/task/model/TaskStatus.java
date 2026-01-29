package com.finalProjectLedZeppelin.task.model;

/**
 * Enumeration of possible task statuses.
 * <p>
 * Represents the lifecycle state of a task.
 */
public enum TaskStatus {

    /**
     * Task has been created but work has not started yet.
     */
    TODO,

    /**
     * Task is currently being worked on.
     */
    IN_PROGRESS,

    /**
     * Task has been completed.
     */
    DONE
}

