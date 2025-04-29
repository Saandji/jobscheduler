package service

import com.samshend.jobscheduler.Scheduler
import com.samshend.jobscheduler.exceptions.ResourceNotFoundException
import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.model.JobStatus
import com.samshend.jobscheduler.service.CoroutineScheduler
import com.samshend.jobscheduler.store.impl.InMemoryJobStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import model.JobDefinitionBuilder
import model.RecurrenceConfiguration
import mu.KotlinLogging
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoroutineSchedulerTest {
    val log = KotlinLogging.logger {}

    private val storage = InMemoryJobStorage()
    private lateinit var scheduler: Scheduler

    @BeforeEach
    fun setup() {
        storage.clear() //clear the storage before each test to ensure no leftovers from previous tests
        scheduler = CoroutineScheduler(
            storage = storage,
        )
    }

    @Test
    fun `schedule one-time job and complete successfully`() {
        runBlocking {
            val jobResult = "Success"
            val id = "job-1"

            val jobDef = givenInstantJob(id, jobResult)
            scheduler.schedule(jobDef)

            // Wait for the job to complete
            delay(100)

            val status = scheduler.getJobStatus(id)
            assertEquals(JobStatus.COMPLETED, status)

            val result = scheduler.awaitResult(id, String::class.java)
            assertEquals(jobResult, result.result)
        }
    }

    @Test
    fun `await result should throw ResourceNotFoundException if job not found`() {
        runBlocking {
            assertThrows<ResourceNotFoundException> {
                scheduler.awaitResult("i don't exist", String::class.java)
            }
        }
    }

    @Test
    fun `schedule delayed job and complete successfully`() = runBlocking {
        val jobResult = "DelayedSuccess"
        val jobId = "job-1"
        val jobDef = givenDelayedJob(jobId, jobResult)

        scheduler.schedule(jobDef)

        assertEquals(jobResult, scheduler.awaitResult(jobId, String::class.java).result)
        assertEquals(JobStatus.COMPLETED, scheduler.getJobStatus(jobId))
    }

    @Test
    fun `should return correct status for DELAYED job`() {
        val scheduledJobWithDelay = givenDelayedJob("job-1")

        scheduler.schedule(scheduledJobWithDelay)

        val status = scheduler.getJobStatus("job-1")
        assertEquals(JobStatus.SCHEDULED, status)
    }

    @Test
    fun `should return correct status for RUNNING job`() {
        runBlocking {
            val expectedJobResult = "StillRunning"
            val jobId = "job-3"
            val scheduledJobWithDelay = givenLongRunningJob(jobId)

            scheduler.schedule(scheduledJobWithDelay)
            //give it a small delay to start running (but not finish)
            delay(100)


            val statusWhileRunning = scheduler.getJobStatus(jobId)
            log.info("Status while running: $statusWhileRunning")

            assertEquals(JobStatus.RUNNING, statusWhileRunning)

            // Wait for the job to finish fully
            val result = scheduler.awaitResult(jobId, String::class.java)
            assertEquals(expectedJobResult, result.result)

            val statusAfterCompletion = scheduler.getJobStatus(jobId)
            assertEquals(JobStatus.COMPLETED, statusAfterCompletion)
        }
    }

    @Test
    fun `should return correct status for COMPLETED job`() {
        runBlocking {
            val expectedJobResult = "StillRunning"
            val jobId = "job-3"
            val scheduledJobWithDelay = givenLongRunningJob(jobId)

            scheduler.schedule(scheduledJobWithDelay)
            //give it a small delay to start running (but not finish)
            delay(100)


            val statusWhileRunning = scheduler.getJobStatus(jobId)
            log.info("Status while running: $statusWhileRunning")

            assertEquals(JobStatus.RUNNING, statusWhileRunning)

            // Wait for the job to finish fully
            val result = scheduler.awaitResult(jobId, String::class.java)
            assertEquals(expectedJobResult, result.result)

            val statusAfterCompletion = scheduler.getJobStatus(jobId)
            assertEquals(JobStatus.COMPLETED, statusAfterCompletion)
        }
    }

    @Test
    fun `getResult throws ResourceNotFoundException if job not found`() {
        val ex = assertThrows<ResourceNotFoundException> {
            scheduler.getResult<String>("non-existent-job", String::class.java)
        }
        assertEquals("Job not found: non-existent-job", ex.message)
    }

    @Test
    fun `should get result of the job`() {
        runBlocking {
            val jobResult = "Success"
            val jobId = "job-1"
            val jobDef = givenInstantJob(id = jobId, jobResult = jobResult)

            scheduler.schedule(jobDef)

            // Wait for the job to complete
            delay(100)

            //check everything is completed
            val status = scheduler.getJobStatus(jobId)
            assertEquals(JobStatus.COMPLETED, status)

            val result = scheduler.awaitResult(jobId, String::class.java)
            assertEquals(jobResult, result.result)

            //get result from the storage
            val resultFromStore = scheduler.getResult(jobDef.id, String::class.java)
            assertEquals(result, resultFromStore.get())
        }
    }

    @Test
    fun `getResult throws IllegalArgumentException on type mismatch`() {
        runBlocking {

            val jobDef = JobDefinition(
                id = "job-4",
                name = "TypeMismatch",
                resultType = String::class.java,
                action = { "Mismatch" }
            )

            val handle = scheduler.schedule(jobDef)
            delay(100)

            assertThrows<IllegalArgumentException> {
                scheduler.getResult<Int>("job-4", Int::class.java)
            }
        }
    }

    @Test
    fun `listJobs returns all scheduled jobs`() {
        val jobDef1 = givenInstantJob("id1", "result1")
        val jobDef2 = givenInstantJob("id2", "result2")
        val jobDef3 = givenInstantJob("id3", "result3")

        scheduler.schedule(jobDef1)
        scheduler.schedule(jobDef2)
        scheduler.schedule(jobDef3)

        val jobs = scheduler.listJobs()

        assertEquals(3, jobs.size)

        val jobIds = jobs.map { it.id }
        assertTrue(jobIds.containsAll(listOf("id1", "id2", "id3")))
    }

    @Test
    fun `cancel running job successfully`() = runBlocking {
        val jobId = "cancel-running-job"

        val jobDef = givenLongRunningJob(jobId)

        scheduler.schedule(jobDef)

        delay(200) // Give it time to start running

        val cancelResult = scheduler.cancelJob(jobId)
        assertTrue(cancelResult)

        delay(200) // Give cancellation time to propagate

        val status = scheduler.getJobStatus(jobId)
        assertEquals(JobStatus.CANCELLED, status)
    }

    @Test
    fun `cancel already completed job returns false`() = runBlocking {
        val jobId = "completed-job"

        val jobDef = givenInstantJob(jobId, "Done")

        scheduler.schedule(jobDef)

        delay(200) // Wait for it to finish

        val statusBefore = scheduler.getJobStatus(jobId)
        assertEquals(JobStatus.COMPLETED, statusBefore)

        val cancelResult = scheduler.cancelJob(jobId)
        assertFalse(cancelResult)

        val statusAfter = scheduler.getJobStatus(jobId)
        assertEquals(JobStatus.COMPLETED, statusAfter) // Should stay completed
    }

    @Test
    fun `schedule job with cron expression throws UnsupportedOperationException`() {
        val cronJobDef = JobDefinition(
            id = "cron-job",
            name = "CronJob",
            recurrenceConfig = RecurrenceConfiguration.cron("0 0 * * * ?"), // Example cron
            resultType = String::class.java,
            action = { "Should not run" }
        )

        val ex = assertThrows<UnsupportedOperationException> {
            scheduler.schedule(cronJobDef)
        }
        assertEquals("Cron recurrence not yet supported", ex.message)
    }

    @Test
    fun `awaitResultBlocking should wait until job finishes`() {
        val scheduler = CoroutineScheduler()

        val job = JobDefinitionBuilder<String>()
            .id("blocking-test")
            .name("BlockingAwait")
            .resultType(String::class.java)
            .recurrence(RecurrenceConfiguration.delayed(Duration.ofMillis(100)))
            .action { "Hello from blocking test" }
            .build()

        scheduler.schedule(job)

        val result = scheduler.awaitResultBlocking("blocking-test", String::class.java)

        assertEquals("Hello from blocking test", result.result)
    }

    @Nested
    @DisplayName("Repeated jobs tests")
    inner class RepeatedJobsTests {

        private val jobResult = "Execution completed"

        @Test
        fun `repeated job increments executionsCompleted properly`() = runBlocking {
            val jobId = "repeated-job-test"
            val jobDef = givenRepeatJob(jobId, jobResult, 3, Duration.ofMillis(100))

            scheduler.schedule(jobDef)

            // Wait enough time for all repetitions to happen
            delay(500) // 3 executions with 100ms interval = ~300ms, so 400ms is safe

            // Find the job in the system
            val jobs = scheduler.listJobs()
            val job = jobs.find { it.id == jobId }
            assertNotNull(job)

            // Now assert that the executionsCompleted = 3
            val record = storage.getJob(jobId)!!

            assertEquals(3, record.executionsCompleted)

            // Also check that status is COMPLETED
            assertEquals(JobStatus.COMPLETED, record.status)
        }

        private fun givenRepeatJob(
            jobId: String,
            jobResult: String,
            times: Int = 3,
            interval: Duration = Duration.ofMillis(100)
        ): JobDefinition<String> {
            val jobDef = JobDefinition(
                id = jobId,
                name = "Repeated Job",
                resultType = String::class.java,
                recurrenceConfig = RecurrenceConfiguration.repeat(
                    times = times,
                    interval = interval
                ),
                action = {
                    jobResult
                }
            )
            return jobDef
        }

        @Test
        fun `scheduler tracks executionsCompleted midway`() = runBlocking {
            val jobId = "midway-job"
            val jobDef = givenRepeatJob(jobId, "Execution completed", 10, Duration.ofMillis(100))

            scheduler.schedule(jobDef)

            // Wait for couple of executions to happen
            delay(450)

            val record = storage.getJob(jobId)!!

            assertTrue(record.executionsCompleted in 2..9) // should fail somewhere in between
        }

        @Test
        fun `scheduler allows cancelling midway`() = runBlocking {
            val jobId = "cancel-midway-job"
            val jobDef = givenRepeatJob(
                jobId,
                "Execution completed",
                10,
                Duration.ofMillis(300)
            )

            val handle = scheduler.schedule(jobDef)

            delay(600) // Let 1-2 executions happen

            val cancelled = scheduler.cancelJob(jobId)
            assertTrue(cancelled)

            delay(200) // Allow cancellation to fully propagate

            val record = storage.getJob(jobId)!!

            assertEquals(JobStatus.CANCELLED, record.status)
            assertTrue(record.executionsCompleted in 1..3)
        }

        @Test
        fun `scheduler handles failure during repeated executions`() = runBlocking {
            val jobId = "failure-midway-job"
            val counter = java.util.concurrent.atomic.AtomicInteger(0)

            val jobDef = JobDefinition(
                id = jobId,
                name = "Fail Midway Job",
                resultType = String::class.java,
                recurrenceConfig = RecurrenceConfiguration.repeat(times = 5, interval = Duration.ofMillis(200)),
                action = {
                    val value = counter.incrementAndGet()
                    //fail it when at least 3 jobs have been started and executed
                    if (value == 3) throw RuntimeException("Boom!")
                    "Run $value".also {
                        log.info("Job $value completed")
                    }
                }
            )

            scheduler.schedule(jobDef)

            delay(1000) // Enough for several attempts

            val record = storage.getJob(jobId)!!

            // Assert that we stored executions properly
            assertEquals(3, record.executionsCompleted) // Should fail at third execution
            assertEquals(JobStatus.FAILED, record.status)

            val thirdResult = record.executionResults[2]
            assertNotNull(thirdResult)
            assertEquals(JobStatus.FAILED, thirdResult?.status)
            assertNotNull(thirdResult?.error)
        }
    }

    private fun givenInstantJob(id: String, jobResult: String): JobDefinition<String> {
        val jobDef = JobDefinition(
            id = id,
            name = "Test Job",
            resultType = String::class.java,
            action = { jobResult }
        )
        return jobDef
    }

    private fun givenLongRunningJob(
        jobId: String = "job-1",
        expectedJObResult: String = "StillRunning"
    ): JobDefinition<String> {
        return JobDefinition(
            id = jobId,
            name = "Status Test",
            recurrenceConfig = RecurrenceConfiguration.once(),
            resultType = String::class.java,
            action = {
                log.info("Long-running job started!")
                delay(1000) // simulate long-running work
                log.info("Long-running job completed!")
                expectedJObResult
            }
        )
    }

    private fun givenDelayedJob(
        jobId: String = "job-1",
        expectedJObResult: String = "StillRunning"
    ): JobDefinition<String> {
        val delay = Duration.ofMillis(1000)

        return JobDefinition(
            id = jobId,
            name = "Status Test",
            recurrenceConfig = RecurrenceConfiguration.delayed(delay),
            resultType = String::class.java,
            action = { expectedJObResult }
        )
    }
}