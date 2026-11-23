package com.pablochomcobar.cloudnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the CloudNotes application.
 *
 * @SpringBootApplication turns on:
 *  - auto-configuration (Spring sets up Tomcat, Thymeleaf, etc. for us)
 *  - component scanning (finds our @Controller, @Service, @Repository, @Configuration classes)
 */
@SpringBootApplication
public class CloudNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudNotesApplication.class, args);
    }
}
