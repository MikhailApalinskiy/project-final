package com.finalProjectLedZeppelin;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 * <p>
 * Bootstraps the Spring Boot application and initializes
 * the application context.
 */
@Log4j2
@SpringBootApplication
public class Application {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        log.info("Application starting...");
        SpringApplication.run(Application.class, args);
        log.info("Application started");
    }

}
