package com.samshend.jobscheduler.retry

import java.time.Duration

sealed class Backoff {
    data class Fixed(val delay: Duration) : Backoff()
    data class Exponential(val initialDelay: Duration, val maxDelay: Duration) : Backoff()
}