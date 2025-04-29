package examples;

import com.samshend.jobscheduler.Scheduler;
import com.samshend.jobscheduler.model.JobDefinition;
import com.samshend.jobscheduler.model.JobResult;
import com.samshend.jobscheduler.service.CoroutineScheduler;
import model.JobDefinitionBuilder;

import java.util.Optional;

public class HelloWorldJobExample {
    public static void main(String[] args) throws Exception {
        Scheduler scheduler = new CoroutineScheduler();

        //define job
        JobDefinition<String> job = new JobDefinitionBuilder<String>()
                .id("hello-job")
                .name("Hello World")
                .resultType(String.class)
                .action(() -> "Hello from Java")
                .build();

        //schedule it
        scheduler.schedule(job);

        //wait for the result
        var waitResult = scheduler.awaitResultBlocking(job.getId(), String.class);
        System.out.println("Final Result: " + waitResult.getResult());

        //or get the result from the storage
        Optional<JobResult<String>> result = scheduler.getResult("hello-job", String.class);
        System.out.println("Final Result from the store: " + result.map(JobResult::getResult).orElse("<no result>"));
    }
}
