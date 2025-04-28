package com.samshend.jobscheduler.service

import com.samshend.jobscheduler.Scheduler
import com.samshend.jobscheduler.exceptions.ResourceNotFoundException
import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.model.JobInstance
import com.samshend.jobscheduler.model.JobResult
import com.samshend.jobscheduler.model.JobStatus
import com.samshend.jobscheduler.retry.RetryExecutor
import com.samshend.jobscheduler.retry.SimpleRetryExecutor
import com.samshend.jobscheduler.service.model.JobRecord
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * CoroutineScheduler is an implementation of the Scheduler interface that leverages Kotlin Coroutines
 * for managing and executing scheduled jobs. It provides concurrency support, job lifecycle tracking,
 * and integration with a retry mechanism through a configurable RetryExecutor.
 *
 * This scheduler allows scheduling jobs with recurrence configurations, tracking job statuses,
 * and fetching job results. Jobs are managed internally with a CoroutineScope for structured
 * concurrency and are executed on a user-specified CoroutineDispatcher.
 *
 * The CoroutineScheduler is suitable for use cases that involve scheduling and managing
 * concurrent tasks with support for failover, retries, and status/result tracking.
 *
 * @constructor Creates a CoroutineScheduler instance with the specified CoroutineDispatcher
 *              and RetryExecutor implementations.
 * @param dispatcher The underlying CoroutineDispatcher used for launching coroutines.
 *                   Defaults to [Dispatchers.Default].
 * @param retryExecutor The RetryExecutor responsible for handling retry logic during job execution.
 *                      Defaults to an instance of [SimpleRetryExecutor].
 *
 * The following features are provided:
 * - Job scheduling with recurrence configurations (delays, intervals, etc.).
 * - Job status querying and tracking capabilities.
 * - Result retrieval with type-safety and retry policies.
 * - Exception handling and safe resource cleanup for coroutine jobs.
 */
class CoroutineScheduler(
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : Scheduler {

    val log = KotlinLogging.logger {}

    /**
     * Defines the retry mechanism for executing jobs within the `CoroutineScheduler`.
     *
     * This property is an instance of `RetryExecutor` and is responsible for handling
     * the execution of job definitions with retry logic applied. It uses a `SimpleRetryExecutor`
     * implementation, which supports retry strategies such as fixed or exponential backoff
     * along with a maximum retry limit.
     *
     * The `retryExecutor` is primarily utilized in job execution workflows where transient
     * failures may occur (e.g., network issues, temporary database errors) and a retry mechanism
     * helps ensure job completion. It ensures retries are performed based on the specified
     * retry policy in the `JobDefinition`, while also managing delays and exceptions.
     *
     * This mechanism is a key part of job scheduling and execution reliability in the scheduler.
     * TODO: note that we have it internally hidden right now, but we expose the interface for possible future customization
     */
    private val retryExecutor: RetryExecutor = SimpleRetryExecutor()

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    //internal for testing purposes
    internal val jobs = ConcurrentHashMap<String, JobRecord>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> schedule(definition: JobDefinition<T>) {
        val record = JobRecord(definition)
        jobs[definition.id] = record
        scheduleInternal(definition, record)
    }

    override fun listJobs(): List<JobInstance> = jobs.values.map { record ->
        JobInstance(
            id = record.definition.id,
            name = record.definition.name,
            status = record.status,
            result = record.result
        )
    }

    override fun getJobStatus(jobId: String): JobStatus {
        val record = jobs[jobId] ?: throw ResourceNotFoundException("Job not found: $jobId")
        return record.status
    }

    override fun <T> getResult(jobId: String, expectedType: Class<T>): Optional<JobResult<T>> {
        val record = jobs[jobId] ?: throw ResourceNotFoundException("Job not found: $jobId")
        val rawResult = record.result ?: return Optional.empty()

        // SAFETY CHECK: Compare declared resultType vs expectedType
        val declaredType = record.definition.resultType
        if (!declaredType.isAssignableFrom(expectedType)) {
            throw IllegalArgumentException(
                "Result type mismatch: job declared ${declaredType.name}, but caller expects ${expectedType.name}"
            )
        }

        return Optional.of(
            JobResult(
                status = rawResult.status,
                expectedType.cast(rawResult.result),
                error = rawResult.error
            )
        )
    }


    /**
     * Schedules a job for execution based on its recurrence configuration.
     *
     * This method handles different types of recurrence configurations, including scheduled
     * delays, repetitions with intervals, or single execution. If a cron-based recurrence
     * is defined, it throws an exception as it's not yet supported.
     *
     * @param T The type of result produced by the job.
     * @param def The job definition containing the details for the job to be scheduled.
     * @param record The job record holding the current status and metadata of the job.
     */
    private fun <T> scheduleInternal(
        def: JobDefinition<T>,
        record: JobRecord
    ) {
        val rec = def.recurrenceConfig

        //TODO: implement cron-based recurrence support
        if (rec.cronExpression != null) {
            throw UnsupportedOperationException("Cron recurrence not yet supported")
        }

        val job = scope.launch {
            //if delay specified, wait for that amount of time before executing the job
            rec.delay?.let { delay(it.toMillis()) }

            //repeat job until cancelled or max times specified
            if (
                rec.repeatTimes != null && rec.interval != null) {
                repeat(rec.repeatTimes) {
                    executeJob(def, record)
                    delay(rec.interval.toMillis())
                }
            } else {
                //otherwise execute the job once immediately
                executeJob(def, record)
            }

            // After all repetitions (or simply the job itself) successfully completed:
            synchronized(record) {
                if (record.status == JobStatus.RUNNING || record.status == JobStatus.SCHEDULED) {
                    log.info("[Scheduler] All repeats completed successfully. Setting status to COMPLETED.")
                    record.status = JobStatus.COMPLETED
                }
            }
        }

        record.job = job
    }

    /**
     * Executes the job and handles the retry mechanism based on the provided retry policy.
     *
     */
    private suspend fun <T> executeJob(
        def: JobDefinition<T>,
        record: JobRecord
    ) {
        record.status = JobStatus.RUNNING
        try {
            val result = retryExecutor.executeWithRetry(def)
            log.info("[CoroutineScheduler] Job ${def.id} execution successful")
            synchronized(record) {
                record.executionResults[record.executionsCompleted] = JobResult(JobStatus.COMPLETED, result, null)
                record.result = JobResult(JobStatus.COMPLETED, result, null)
                record.executionsCompleted++
            }
        } catch (e: Throwable) {
            when (e) {
                is CancellationException -> {
                    log.info("[CoroutineScheduler] Job ${def.id} was cancelled")
                    synchronized(record) {
                        record.executionResults[record.executionsCompleted] = JobResult(JobStatus.CANCELLED, null, e)
                        record.result = JobResult(JobStatus.CANCELLED, null, e)
                        record.status = JobStatus.CANCELLED
                        record.executionsCompleted++
                    }
                }

                else -> {
                    log.info("[CoroutineScheduler] Job ${def.id} failed with exception: $e")
                    synchronized(record) {
                        record.executionResults[record.executionsCompleted] = JobResult(JobStatus.FAILED, null, e)
                        record.result = JobResult(JobStatus.FAILED, null, e)
                        record.status = JobStatus.FAILED
                        record.executionsCompleted++
                        //throw exception and stop execution of the job
                        //TODO: possible extra configuration to continue execution after exception?
                        throw e
                    }
                }
            }
        }
    }

    /**
     * Attempts to cancel the underlying coroutine.
     */
    override fun cancelJob(jobId: String): Boolean {
        val record = jobs[jobId] ?: throw ResourceNotFoundException("Job not found: $jobId")
        val job = record.job ?: return false

        return if (job.isActive) {
            job.cancel()

            synchronized(record) {
                log.info("[CoroutineScheduler] Cancelling job $jobId")
                if (record.status == JobStatus.SCHEDULED || record.status == JobStatus.RUNNING) {
                    record.status = JobStatus.CANCELLED
                }
            }

            true
        } else {
            false
        }
    }

    override suspend fun <T> awaitResult(jobId: String, expectedType: Class<T>): JobResult<T> {
        val record = jobs[jobId] ?: throw ResourceNotFoundException("Job not found: $jobId")

        record.job?.join()

        val rawResult = record.result
            ?: throw IllegalStateException("Result not available for job: $jobId")

        return JobResult(
            status = rawResult.status,
            result = expectedType.cast(rawResult.result),
            error = rawResult.error
        )
    }
}