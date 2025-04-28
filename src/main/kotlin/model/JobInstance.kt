package com.samshend.jobscheduler.model

/**
 * Represents an instance of a job within the job scheduler.
 *
 * This class encapsulates the details of a scheduled or completed job, including its unique identifier,
 * name, current status, optional result of the executed job, and the timestamp when the job was created.
 *
 * @property id A unique identifier for this job instance.
 * @property name A descriptive name of the job instance, useful for identification and debugging.
 * @property status The current execution status of the job (e.g., RUNNING, COMPLETED, FAILED, SCHEDULED, CANCELLED).
 * @property result The optional result of the job's execution. Null if the job is still running or has not produced a result.
 * @property createdAt The timestamp representing when this job instance was created. Defaults to the current system time.
 */
data class JobInstance(
    val id: String,
    val name: String,
    val status: JobStatus,
    val result: JobResult<*>? = null,
    val createdAt: Long = System.currentTimeMillis()
)