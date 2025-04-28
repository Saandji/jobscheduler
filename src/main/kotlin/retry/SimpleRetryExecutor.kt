package com.samshend.jobscheduler.retry

import com.samshend.jobscheduler.model.JobDefinition
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Implementation of the RetryExecutor interface that handles retry logic for executing jobs.
 *
 * The SimpleRetryExecutor executes a given job definition and attempts to retry the execution
 * in case of failures based on the defined retry policy. The retry policy can include a backoff
 * strategy (fixed or exponential) and a maximum number of retry attempts. If all retry attempts fail,
 * the last exception encountered during execution is rethrown.
 *
 * The backoff strategies supported are:
 * - Fixed: A constant delay between retries.
 * - Exponential: A delay that increases exponentially with each retry, up to a maximum delay.
 *
 * If no exception is thrown during the retries, the job execution result is returned immediately.
 *
 * This class is designed to be used where retry logic is crucial, such as handling transient failures
 * in network calls, database transactions, or other unstable operations.
 *
 * @constructor Instantiates a SimpleRetryExecutor.
 */
class SimpleRetryExecutor : RetryExecutor {
    override suspend fun <T> executeWithRetry(definition: JobDefinition<T>): T {
        var attempt = 1
        var lastError: Throwable? = null
        while (attempt <= definition.retryPolicy.maxAttempts) {
            try {
                return definition.action()
            } catch (e: Throwable) {
                lastError = e
                if (attempt == definition.retryPolicy.maxAttempts) break
                val waitMillis = when (val backoff = definition.retryPolicy.backoff) {
                    is Backoff.Fixed -> backoff.delay.toMillis()
                    is Backoff.Exponential -> {
                        val base = backoff.initialDelay.toMillis()
                        val exp = (base * 2.0.pow(attempt - 1)).toLong()
                        min(exp, backoff.maxDelay.toMillis())
                    }
                }
                delay(waitMillis)
            }
            attempt++
        }
        throw lastError ?: IllegalStateException("Job failed without an exception")
    }
}