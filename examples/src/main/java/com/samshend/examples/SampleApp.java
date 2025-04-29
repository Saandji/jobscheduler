package com.samshend.examples;

import com.samshend.jobscheduler.Scheduler;
import com.samshend.jobscheduler.model.JobStatus;
import com.samshend.jobscheduler.service.CoroutineScheduler;
import model.JobDefinitionBuilder;
import model.RecurrenceConfiguration;

public class SampleApp {
    public static void main(String[] args) throws Exception {
        Scheduler scheduler = new CoroutineScheduler();

        var job = new JobDefinitionBuilder<String>()
                .id("java-job-1")
                .name("JavaJob")
                .resultType(String.class)
                .recurrence(RecurrenceConfiguration.once())
                .action(() -> {
                    System.out.println("[Java] Job running...");
                    try {
                        Thread.sleep(1000); // simulate work
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "Completed from Java!";
                })
                .build();

        scheduler.schedule(job);

        while (true) {
            JobStatus status = scheduler.getJobStatus(job.getId());
            System.out.println("[Java] Status: " + status);

            if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
                break;
            }

            Thread.sleep(500);
        }

        var result = scheduler.awaitResultBlocking(job.getId(), String.class);
        System.out.println("[Java] Final result: " + result.getResult());
    }
}