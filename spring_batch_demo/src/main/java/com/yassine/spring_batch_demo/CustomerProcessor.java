package com.yassine.spring_batch_demo;

import com.yassine.spring_batch_demo.entity.Customer;
import org.springframework.batch.infrastructure.item.ItemProcessor;


import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class CustomerProcessor implements ItemProcessor<Customer, Customer> {

    public static int count = 0;
    @Override
    public Customer process(Customer customer) throws Exception {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");

        LocalDate dob = LocalDate.parse(customer.getDob(), formatter);
        int age = Period.between(dob, LocalDate.now()).getYears();

        if (age < 18) {
            return null;
        }
        count++;
        double originalBalance = customer.getBalance();
        double bonus = originalBalance * 0.10;
        double finalBalance = originalBalance + bonus;

        customer.setBalance(finalBalance);

        System.out.println("✅ Accepted: " + customer.getFirstName() +
                " | Age: " + age +
                " | Old Balance: " + originalBalance +
                " | New Balance (+10%): " + finalBalance);
        return customer;
    }
}