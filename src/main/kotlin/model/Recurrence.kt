package com.samshend.jobscheduler.model

import java.time.Duration


/**
 * Represents the recurrence schedule of a job.
 *
 * This sealed class defines the recurrence behavior for a job, which could range from a one-time execution to
 * delayed or cron-based scheduling. Each specific recurrence type is encapsulated in its respective subclass.
 */
sealed class Recurrence {
    /**
     * Represents a non-repeating occurrence in a scheduling context.
     *
     * This object is a concrete implementation of the `Recurrence` sealed class, used to define jobs
     * or activities that are executed exactly once. It is typically used when no recurrence is required
     * for a scheduled job.
     */
    object Once : Recurrence()

    /**
     * Represents a delayed recurrence for a job scheduling system.
     *
     * This class is a part of the sealed `Recurrence` hierarchy and specifies
     * that a job should be executed after a certain delay.
     *
     * @property delay The duration of the delay before the job is executed.
     */
    data class Delayed(val delay: Duration) : Recurrence()

    /**
     * Represents a cron-based recurrence strategy for a job.
     *
     * This class is a specific type of [Recurrence], utilizing a cron expression
     * to define the timing and repetition rules for job execution.
     *
     * @property expression The cron expression dictating the recurrence schedule.
     */
    data class Cron(val expression: String) : Recurrence()

    /**
     * Represents a recurrence where a job is executed a fixed number of times with an optional delay between executions.
     *
     * @property times Number of times the job should run.
     * @property interval Duration between each execution.
     */
    data class FixedCount(val times: Int, val interval: Duration) : Recurrence()
}