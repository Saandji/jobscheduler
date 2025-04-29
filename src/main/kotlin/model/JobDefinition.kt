package com.samshend.jobscheduler.model

import com.samshend.jobscheduler.retry.RetryPolicy
import kotlinx.coroutines.suspendCancellableCoroutine
import model.RecurrenceConfiguration
import java.util.function.Supplier
import kotlin.coroutines.resume


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
class JobDefinition<T> @JvmOverloads constructor(
    val id: String,
    val name: String,
    val recurrenceConfig: RecurrenceConfiguration = RecurrenceConfiguration.once(),
    val retryPolicy: RetryPolicy = RetryPolicy.noRetries,
    val resultType: Class<T>,
    val action: suspend () -> T,
) {

    /**
     * Java-friendly constructor: accepts a Supplier instead of suspend function.
     */
    constructor(
        id: String,
        name: String,
        recurrenceConfig: RecurrenceConfiguration,
        retryPolicy: RetryPolicy,
        resultType: Class<T>,
        supplier: Supplier<T>
    ) : this(
        id,
        name,
        recurrenceConfig,
        retryPolicy,
        resultType,
        action = {
            suspendCancellableCoroutine { cont ->
                try {
                    val result = supplier.get()
                    cont.resume(result)
                } catch (e: Throwable) {
                    cont.resumeWith(Result.failure(e))
                }
            }
        }
    )
}