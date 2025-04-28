package retry

import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.retry.Backoff
import com.samshend.jobscheduler.retry.RetryPolicy
import com.samshend.jobscheduler.retry.SimpleRetryExecutor
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class SimpleRetryExecutorTest {
    val log = KotlinLogging.logger {}
    private val executor = SimpleRetryExecutor()

    @Test
    fun `should execute job successfully without retries`() = runBlocking {
        val jobDef = JobDefinition(
            id = "success-job",
            name = "SuccessJob",
            resultType = String::class.java,
            action = { "Done" }
        )

        val result = executor.executeWithRetry(jobDef)
        assertEquals("Done", result)
    }

    @Test
    fun `should retry once and succeed`() = runBlocking {
        var attempt = 0
        val jobDef = JobDefinition(
            id = "retry-job",
            name = "RetryJob",
            resultType = String::class.java,
            retryPolicy = RetryPolicy(
                backoff = Backoff.Fixed(Duration.ofMillis(100)),
                maxAttempts = 3
            ),
            action = {
                log.info("[RetryJob] Attempt $attempt")
                attempt++
                if (attempt < 2) throw RuntimeException("First attempt failed")
                "SuccessAfterRetry"
            }
        )

        val result = executor.executeWithRetry(jobDef)
        assertEquals("SuccessAfterRetry", result)
        assertEquals(2, attempt)
    }

    @Test
    fun `should fail after all retries exhausted`() = runBlocking {
        var attempt = 0
        val jobDef = JobDefinition(
            id = "fail-job",
            name = "FailJob",
            resultType = String::class.java,
            retryPolicy = RetryPolicy(
                backoff = Backoff.Fixed(Duration.ofMillis(100)),
                maxAttempts = 2
            ),
            action = {
                log.info("[FailJob] Attempt $attempt")
                attempt++
                throw RuntimeException("Always failing")
            }
        )

        val ex = assertThrows<RuntimeException> {
            runBlocking {
                executor.executeWithRetry(jobDef)
            }
        }
        assertEquals("Always failing", ex.message)
        assertEquals(2, attempt) // Tried twice
    }

    @Test
    fun `should retry with exponential backoff and succeed`() = runBlocking {
        var attempt = 0

        val jobDef = JobDefinition(
            id = "exponential-retry-job",
            name = "ExponentialRetryJob",
            resultType = String::class.java,
            retryPolicy = RetryPolicy(
                backoff = Backoff.Exponential(
                    initialDelay = Duration.ofMillis(100),
                    maxDelay = Duration.ofMillis(1000)
                ),
                maxAttempts = 4
            ),
            action = {
                log.info("[ExponentialRetryJob] Attempt $attempt")
                attempt++
                if (attempt < 3) throw RuntimeException("Failing attempt $attempt")
                "SuccessAfterExponentialRetries"
            }
        )

        val result = executor.executeWithRetry(jobDef)
        assertEquals("SuccessAfterExponentialRetries", result)
        assertEquals(3, attempt) // 2 failures + 1 success
    }
}