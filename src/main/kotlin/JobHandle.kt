package com.samshend.jobscheduler

import com.samshend.jobscheduler.model.JobResult
import com.samshend.jobscheduler.model.JobStatus

/**
 * Represents a handle for interacting with a scheduled job.
 *
 * A `JobHandle` provides methods to manage and monitor the lifecycle of a job. This includes
 * the ability to cancel the job, check its current status, and await its result
 * upon completion or termination.
 */
interface JobHandle {
    /**
     * Cancels the associated job.
     *
     * This method attempts to terminate the ongoing execution of the job. If the job is
     * currently running or scheduled, it transitions to a cancelled state. Cancellation
     * is a best-effort operation and may not interrupt jobs that are already completed
     * or finalized.
     *
     * @return `true` if the cancellation was successfully initiated or the job was already cancelled,
     *         `false` if the job could not be cancelled (e.g., already completed or invalid state).
     */
    fun cancel(): Boolean

    /**
     * Retrieves the current status of the job associated with this handle.
     *
     * The status indicates the lifecycle state of the job, such as whether it is running,
     * completed, failed, scheduled, or cancelled.
     *
     * @return The current status of the job as a value of the [JobStatus] enum.
     */
    fun getStatus(): JobStatus

    /**
     * Awaits the completion of the job and retrieves its result.
     *
     * This method blocks until the job has completed, failed, or has been explicitly
     * cancelled. The result of the job is returned encapsulated within a `JobResult` object,
     * which provides information on the job's outcome, result (if successful), or any error
     * encountered during execution. The result is type-checked against the provided expected type.
     *
     * @param T The type of the result expected from the job.
     * @param expectedType The class of the expected result type. Used to validate the type
     *                     of the result produced by the job.
     * @return A `JobResult` object that contains the outcome of the job, which can
     *         include the resulting value, the status of the job, or any exception that
     *         occurred during execution.
     */
    fun <T> await(expectedType: Class<T>): JobResult<T>
}