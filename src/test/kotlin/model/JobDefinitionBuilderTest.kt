package model

import com.samshend.jobscheduler.retry.RetryPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.function.Supplier
import kotlin.test.assertFailsWith

class JobDefinitionBuilderTest {
    @Test
    fun `should build a valid JobDefinition`() {
        val job = JobDefinitionBuilder<String>()
            .id("job-1")
            .name("My Job")
            .resultType(String::class.java)
            .recurrence(RecurrenceConfiguration.delayed(Duration.ofSeconds(1)))
            .retryPolicy(RetryPolicy.noRetries)
            .action(Supplier { "Hello" })
            .build()

        assertEquals("job-1", job.id)
        assertEquals("My Job", job.name)
        assertEquals(String::class.java, job.resultType)
    }

    @Test
    fun `should fail to build when id is missing`() {
        assertFailsWith<IllegalStateException> {
            JobDefinitionBuilder<String>()
                .name("Missing ID")
                .resultType(String::class.java)
                .action(Supplier { "Hello" })
                .build()
        }
    }

    @Test
    fun `should fail to build when resultType is missing`() {
        assertFailsWith<IllegalStateException> {
            JobDefinitionBuilder<String>()
                .id("job-2")
                .name("Missing resultType")
                .action(Supplier { "Hello" })
                .build()
        }
    }

    @Test
    fun `should fail to build when action is missing`() {
        assertFailsWith<IllegalStateException> {
            JobDefinitionBuilder<String>()
                .id("job-3")
                .name("Missing action")
                .resultType(String::class.java)
                .build()
        }

    }
}