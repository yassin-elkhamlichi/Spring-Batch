package com.yassine.spring_batch_demo.config;

import com.yassine.spring_batch_demo.CustomerProcessor;
import com.yassine.spring_batch_demo.entity.Customer;
import com.yassine.spring_batch_demo.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@AllArgsConstructor
public class SpringBatchConfig {
    private CustomerRepository customerRepository;

    // 1 READER
    @Bean
    public FlatFileItemReader<Customer> reader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("csvReader")
                .resource(new ClassPathResource("db/customers.csv"))
                .linesToSkip(0)
                .lineMapper(lineMapper())
                .build();
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName",
                "email", "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;

    }


    // 2 PROCESSOR
    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }


    // 3 WRITER
    @Bean
    public RepositoryItemWriter<Customer> writer() {
       return new RepositoryItemWriterBuilder<Customer>()
               .repository(customerRepository)
               .methodName("save")
               .build();
    }
    // 4 STEP
    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("csv-import-step", jobRepository)
                .<Customer, Customer>chunk(100)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .faultTolerant() // ENABLE FAULT TOLERANCE
                .retry(ObjectOptimisticLockingFailureException.class) // RETRY ON LOCKING FAILURES
                .retryLimit(3) // MAX 3 RETRIES
                .transactionManager(platformTransactionManager)
                .build();
    }

    // 5 JOB

    @Bean
    public Job runJob(JobRepository jobRepository , PlatformTransactionManager transactionManager){
        return new JobBuilder("importCustomers", jobRepository)
                .flow(step1(jobRepository, transactionManager))
                .end()
                .build();
    }
}
