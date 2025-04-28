package com.samshend.jobscheduler.retry

import java.time.Duration

data class RetryPolicy(
    val backoff: Backoff = Backoff.Fixed(Duration.ofMillis(DEFAULT_INITIAL_DELAY_MILLIS)),
    val maxAttempts: Int = DEFAULT_MAX_RETRIES
) {
    companion object {
        private const val DEFAULT_INITIAL_DELAY_MILLIS = 1000L
        private const val DEFAULT_MAX_RETRIES = 3

        /**
         * No retries: only one attempt.
         */
        val noRetries: RetryPolicy = RetryPolicy(
            backoff = Backoff.Fixed(Duration.ZERO),
            maxAttempts = 1
        )

        /**
         * Default retries: few attempts with backoff.
         */
        val defaultRetries: RetryPolicy = RetryPolicy(
            backoff = Backoff.Fixed(Duration.ofMillis(DEFAULT_INITIAL_DELAY_MILLIS)),
            maxAttempts = DEFAULT_MAX_RETRIES
        )
    }
}