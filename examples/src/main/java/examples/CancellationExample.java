package examples;

import com.samshend.jobscheduler.Scheduler;
import com.samshend.jobscheduler.model.JobDefinition;
import com.samshend.jobscheduler.model.JobResult;
import com.samshend.jobscheduler.model.JobStatus;
import com.samshend.jobscheduler.service.CoroutineScheduler;
import model.JobDefinitionBuilder;
import model.RecurrenceConfiguration;

import java.time.Duration;
import java.util.Optional;

public class CancellationExample {
    public static void main(String[] args) throws Exception {
        Scheduler scheduler = new CoroutineScheduler();

        // Define a long-running job
        JobDefinition<String> job = new JobDefinitionBuilder<String>()
                .id("cancel-job")
                .name("Cancellable Job")
                .resultType(String.class)
                .recurrence(RecurrenceConfiguration.delayed(Duration.ofSeconds(1)))
                .action(() -> {
                    System.out.println("Started long job...");
                    try {
                        Thread.sleep(5000); // simulate long-running job
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "Done after long work";
                })
                .build();

        // Schedule the job
        scheduler.schedule(job);

        // Wait a bit, then cancel it
        Thread.sleep(1500);
        scheduler.cancelJob("cancel-job");
        System.out.println("Requested cancellation");

        // Poll until the job finishes with any status
        while (true) {
            JobStatus status = scheduler.getJobStatus("cancel-job");
            System.out.println("Status: " + status);
            if (status == JobStatus.COMPLETED || status == JobStatus.FAILED || status == JobStatus.CANCELLED) {
                break;
            }
            Thread.sleep(500);
        }

        // Get the result if any
        Optional<JobResult<String>> result = scheduler.getResult("cancel-job", String.class);
        System.out.println("Final Result: " + result.map(r -> r.getStatus().name()).orElse("<no result>"));
    }
}