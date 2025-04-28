package com.samshend.jobscheduler.model

import com.samshend.jobscheduler.retry.RetryPolicy
import model.RecurrenceConfiguration


/**
 * Represents a definition of a job to be scheduled.
 *
 * A `JobDefinition` encapsulates all the necessary details required for scheduling and executing
 * a recurring, potentially retryable job, including its unique identifier, descriptive name,
 * recurrence strategy, retry policy, and the action to be performed.
 *
 * @param T The type of result produced by the job's action upon successful completion.
 * @property id A unique identifier for the job.
 * @property name A descriptive name for the job, useful for logging and debugging purposes.
 * @property recurrenceConfig Specifies the recurrence strategy for the job execution. Defaults to `RecurrenceConfiguration.once()`.
 * @property retryPolicy Defines the behavior for handling job retries in case of failures. Defaults to a single attempt e.g. no retries.
 * @property action A suspending function that encapsulates the logic of the job to be executed.
 */
class JobDefinition<T>(
    val id: String,
    val name: String,
    val recurrenceConfig: RecurrenceConfiguration = RecurrenceConfiguration.once(),
    val retryPolicy: RetryPolicy = RetryPolicy.noRetries,
    //TODO: Still not 100% sure we want and need this type, but I keep it for until I test with pure java
    val resultType: Class<T>,
    val action: suspend () -> T,
)