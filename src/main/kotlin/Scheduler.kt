package com.samshend.jobscheduler

import com.samshend.jobscheduler.model.*
import java.util.*

/**
 * Defines the contract for a scheduler capable of managing job execution.
 *
 * The `Scheduler` interface facilitates the scheduling, execution, tracking,
 * and management of jobs. Jobs are defined using a `JobDefinition` object,
 * which encapsulates all necessary details for their execution such as a unique
 * identifier, recurrence configuration, retry policy, and the action to be executed.
 *
 * This interface supports job lifecycle operations, including status tracking,
 * result retrieval, cancellation, and awaiting job completion. Jobs can produce
 * typed results and are executed as asynchronous tasks.
 */
interface Scheduler {

    /**
     * Schedules a job for execution by defining its properties and logic through a builder.
     *
     * This method provides a DSL-style interface for creating a job definition, allowing
     * the caller to specify properties such as the job's unique identifier, name, recurrence
     * strategy, retry policy, and execution logic. The constructed job is then scheduled
     * for execution.
     *
     * @param T The type of result expected from the job's successful execution.
     * @param block A lambda with a receiver of type `JobDefinitionBuilder<T>` used to define
     *              the job's details including its identifier, name, recurrence strategy,
     *              retry policy, result type, and execution action.
     */
    fun <T> scheduleJob(block: JobDefinitionBuilder<T>.() -> Unit) {
        val def = jobDefinition(block)
        return schedule(def)
    }

    /**
     * Schedules a job for execution based on the provided job definition.
     *
     * This method allows the scheduling of a job by encapsulating its details
     * such as identifier, recurrence strategy, retry policy, and the logic to
     * be executed upon job execution.
     *
     * @param T The type of result produced upon successful execution of the job.
     * @param definition The `JobDefinition` that encapsulates all the necessary details
     *                   required to schedule and execute the job.
     *                   It includes properties such as the job's unique identifier,
     *                   descriptive name, recurrence strategy, retry policy, and action.
     */
    fun <T> schedule(definition: JobDefinition<T>)

    /**
     * Retrieves a list of all currently scheduled or completed job instances managed by the scheduler.
     *
     * The returned list includes details such as the job's unique identifier, name, current status,
     * optional result, and creation timestamp. This method provides a snapshot of the job lifecycle
     * within the scheduler at the time of invocation.
     *
     * @return A list of `JobInstance` objects representing the jobs currently tracked by the scheduler.
     */
    fun listJobs(): List<JobInstance>

    /**
     * Retrieves the current status of a job using its unique identifier.
     *
     * This method is used to check the status of a job that has been
     * scheduled or is in progress. The returned status indicates
     * whether the job is running, completed, failed, scheduled, or cancelled.
     *
     * @param jobId The unique identifier of the job whose status
     *              is to be retrieved.
     * @return The current status of the job as a value of the [JobStatus] enum.
     */
    fun getJobStatus(jobId: String): JobStatus

    /**
     * Retrieves the result of a job execution identified by a specific job ID.
     *
     * This method returns an optional encapsulated result of the job, including its execution outcome,
     * the produced result (if any), and any encountered exception. The result is typed to the expected
     * result type provided.
     *
     * @param T The type of result expected from the job execution.
     * @param jobId The unique identifier of the job whose result is to be retrieved.
     * @param expectedType The class of the expected result type. Used to ensure type safety when fetching the result.
     * @return An `Optional` containing a `JobResult` object that encapsulates the execution details and output of the job.
     *         If the job does not exist or has not completed, an empty `Optional` is returned.
     */
    fun <T> getResult(jobId: String, expectedType: Class<T>): Optional<JobResult<T>>

    /**
     * Cancels the associated job.
     *
     * This method attempts to terminate the ongoing execution of the job. If the job is
     * currently running or scheduled, it transitions to a cancelled state. Cancellation
     * is a best-effort operation and may not interrupt jobs that are already completed
     * or finalized.
     *
     * @param jobId The unique identifier of the job to be cancelled.
     * @return `true` if the cancellation was successfully initiated or the job was already cancelled,
     *         `false` if the job could not be cancelled (e.g., already completed or invalid state).
     */
    fun cancelJob(jobId: String): Boolean

    /**
     * Awaits the completion of the job and retrieves its result.
     *
     * This method blocks until the job has completed, failed, or has been explicitly
     * cancelled. The result of the job is returned encapsulated within a `JobResult` object,
     * which provides information on the job's outcome, result (if successful), or any error
     * encountered during execution. The result is type-checked against the provided expected type.
     *
     * @param T The type of the result expected from the job.
     * @param jobId The unique identifier of the job whose result is to be retrieved.
     * @param expectedType The class of the expected result type. Used to validate the type
     *                     of the result produced by the job.
     * @return A `JobResult` object that contains the outcome of the job, which can
     *         include the resulting value, the status of the job, or any exception that
     *         occurred during execution.
     */
    @JvmSynthetic
    suspend fun <T> awaitResult(jobId: String, expectedType: Class<T>): JobResult<T>

    /**
     * Blocks the current thread until the job identified by the given ID completes
     * and retrieves the result of the job execution.
     *
     * This method should be used when a blocking operation is acceptable
     * to wait for a job's completion. The result is encapsulated within a `JobResult`
     * object, which provides information such as the job status, the result
     * (if the job completed successfully), or any exception encountered during execution.
     *
     * @param T The type of the result expected from the job.
     * @param jobId The unique identifier of the job whose result is awaited.
     * @param expectedType The class of the expected result type. Used to validate the
     *                     type of the result produced by the job.
     * @return A `JobResult` object that contains the outcome of the job, which
     *         could include the resulting value, the job status, or any exception
     *         that occurred during execution.
     */
    fun <T> awaitResultBlocking(jobId: String, expectedType: Class<T>): JobResult<T>
}