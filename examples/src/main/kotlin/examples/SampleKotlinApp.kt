package examples

import com.samshend.jobscheduler.model.JobDefinition
import com.samshend.jobscheduler.model.JobStatus
import com.samshend.jobscheduler.service.CoroutineScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import model.RecurrenceConfiguration
import mu.KotlinLogging
import java.time.Duration

fun main() = runBlocking {
    val log = KotlinLogging.logger {}
    val scheduler = CoroutineScheduler()

    // ---- Job 1: Normal finishing job ----
    scheduler.schedule(
        JobDefinition(
            id = "job-1",
            name = "QuickJob",
            resultType = String::class.java,
            recurrenceConfig = RecurrenceConfiguration.delayed(Duration.ofSeconds(2)),
            action = {
                log.info("[Job1] Executing...")
                delay(1000) // simulate doing some work
                "Job1 Completed Successfully"
            }
        )
    )

    log.info("[Main] Scheduled Job1")

    // ---- Job 2: Will cancel while running ----
    scheduler.schedule(
        JobDefinition(
            id = "job-2",
            name = "CancellableJob",
            resultType = String::class.java,
            recurrenceConfig = RecurrenceConfiguration.delayed(Duration.ofSeconds(1)),
            action = {
                log.info("[Job2] Executing long task...")
                repeat(10) { i ->
                    log.info("[Job2] Working... step $i")
                    delay(5000) // simulate long work
                }
                "Job2 Completed (unexpected!)"
            }
        )
    )

    log.info("[Main] Scheduled Job2")

    // ---- Monitor loop: wait for Job1 to finish ----
    while (true) {
        val status1 = scheduler.getJobStatus("job-1")
        log.info("[Main] Checking Job1 Status: $status1")
        if (status1 == JobStatus.COMPLETED || status1 == JobStatus.FAILED) {
            break
        }
        delay(500)
    }

    val result1 = scheduler.awaitResult("job-1", String::class.java)
    log.info("[Main] Job1 Result: ${result1.result}")

    // ---- Cancel Job2 while running ----
    delay(2500) // wait for Job2 to be in the middle of running
    val cancelResult = scheduler.cancelJob("job-2")
    log.info("[Main] Called cancel on Job2: $cancelResult")

    // ---- Monitor loop: wait for Job2 to finish or be cancelled ----
    while (true) {
        val status2 = scheduler.getJobStatus("job-2")
        log.info("[Main] Checking Job2 Status: $status2")
        if (status2 == JobStatus.CANCELLED || status2 == JobStatus.FAILED || status2 == JobStatus.COMPLETED) {
            break
        }
        delay(500)
    }

    // ---- Try to await Job2 (should handle cancellation gracefully) ----
    try {
        val result2 = scheduler.awaitResult("job-2", String::class.java)
        log.info("[Main] Job2 Result: ${result2.result}")
    } catch (e: Exception) {
        log.info("[Main] Job2 await failed: ${e.message}")
    }
}