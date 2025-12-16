package com.yassine.spring_batch_demo.controller;



import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("job")
public class JobController {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job job;

    @PostMapping("start")
    public String startJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobOperator.start(job, jobParameters);




            while (execution.isRunning()) {
                System.out.println("Job Started with ID: " + execution.getId());
                System.out.println("... Processing ... Current Status: " + execution.getStatus());
                Thread.sleep(1000);
            }


            return "JOB FINISHED with Status: " + execution.getStatus();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}