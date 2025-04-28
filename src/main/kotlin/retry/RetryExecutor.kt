package com.samshend.jobscheduler.retry

import com.samshend.jobscheduler.model.JobDefinition

interface RetryExecutor {
    suspend fun <T> executeWithRetry(definition: JobDefinition<T>): T
}