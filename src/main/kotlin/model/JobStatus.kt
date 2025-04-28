package com.samshend.jobscheduler.model

/**
 * Represents the possible states of a job within the job scheduler.
 *
 * The job state transitions occur based on its lifecycle, such as when a job
 * is being executed, completed successfully, fails, gets scheduled, or
 * is explicitly cancelled.
 *
 *
 *
 * ## Job lifecycle:
 *
 * ```
 *               +-----------+
 *               | SCHEDULED |---------------
 *               +-----------+              |
 *                     |                    |
 *                     v                    |
 *               +-----------+              |
 *               |  RUNNING  | --------     |
 *               +-----------+         |    |
 *              /      |               |    |
 *             v       v               v    v
 * +-----------+    +-----------+   +-----------+
 * | COMPLETED |    |  FAILED   |   | CANCELLED |
 * +-----------+    +-----------+   +-----------+
 * ```
 *
 * ### State transitions:
 * - **SCHEDULED → RUNNING**: When the job starts executing.
 * - **SCHEDULED → CANCELLED**: If the job is cancelled before it starts running.
 * - **RUNNING → COMPLETED**: When the job finishes successfully.
 * - **RUNNING → FAILED**: If an exception occurs during execution.
 * - **RUNNING → CANCELLED**: If the job is cancelled during execution.
 *
 * Terminal states: **COMPLETED**, **FAILED**, **CANCELLED**.
 */
enum class JobStatus {
    /**
     * Indicates that a job is currently in progress.
     *
     * A job with this status is actively running and has not yet reached a terminal state
     * such as `COMPLETED`, `FAILED`, or `CANCELLED`.
     */
    RUNNING,

    /**
     * Indicates that the job has successfully completed its execution.
     *
     * This status is typically set after a job finishes execution without
     * encountering any errors or cancellations.
     */
    COMPLETED,

    /**
     * Represents the state where a job execution has failed.
     *
     * The `FAILED` status indicates that the job could not complete successfully due to an error or exception
     * during its execution. This state is typically terminal.
     */
    FAILED,

    /**
     * Represents a job that has been scheduled but not yet started.
     *
     * The `SCHEDULED` status is used to indicate that the job is queued
     * for execution and will commence according to its defined scheduling
     * and recurrence rules. A job in this state has not yet entered execution.
     */
    SCHEDULED,

    /**
     * Represents the state of a job that has been cancelled.
     *
     * A job transitions to the `CANCELLED` state when an explicit cancellation request is made,
     * ensuring the execution halts if it hasn't been completed yet. It indicates that the job
     * is no longer active or scheduled for execution.
     */
    CANCELLED
}