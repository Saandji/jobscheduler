package com.samshend.jobscheduler.model

/**
 * Represents the result of a job execution.
 *
 * This class encapsulates the outcome of a job, including its status, optional result,
 * and any exception encountered during execution.
 *
 * @param T The type of the result produced by the job upon successful completion.
 * @property status The current status of the job (e.g., RUNNING, COMPLETED, FAILED, SCHEDULED).
 * @property result The result produced by the job upon successful completion. Null if the job failed or is still running.
 * @property error The exception encountered during job execution, if any. Null if the job completed successfully.
 */
data class JobResult<T>(
    val status: JobStatus,
    val result: T?,
    val error: Throwable?
)