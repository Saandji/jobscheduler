package com.samshend.jobscheduler.service.model

import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.model.JobResult
import com.samshend.jobscheduler.model.JobStatus
import kotlinx.coroutines.Job

/**
 * Represents the runtime metadata and state of a scheduled job within the job scheduler.
 *
 * A `JobRecord` serves as a container for holding information about the job's definition,
 * current status, execution results, and execution history. It tracks the progress
 * and state transitions of the job across its lifecycle.
 *
 * JobRecord is internal to [com.samshend.jobscheduler.service.CoroutineScheduler] and should not be used outside the service.
 * For client-facing APIs, use [com.samshend.jobscheduler.model.JobInstance] instead.
 *
 *
 *
 * @property definition The original definition of the job, which includes its configuration and action to execute.
 * @property status The current state of the job, such as SCHEDULED, RUNNING, COMPLETED, FAILED, or CANCELLED.
 * @property result The result of the job's most recent execution, including its status, output, or any encountered error.
 * @property job The underlying coroutine job handling the execution. This is volatile to handle concurrent state updates.
 * @property executionsCompleted Count of how many executions of the job have been completed successfully.
 * @property executionResults A map storing the results of each execution, keyed by execution count.
 */
data class JobRecord(
    val definition: JobDefinition<*>,
    @Volatile var status: JobStatus = JobStatus.SCHEDULED,
    @Volatile var result: JobResult<*>? = null,
    @Volatile var job: Job? = null,
    @Volatile var executionsCompleted: Int = 0,
    @Volatile var executionResults: MutableMap<Int, JobResult<*>> = mutableMapOf()
)