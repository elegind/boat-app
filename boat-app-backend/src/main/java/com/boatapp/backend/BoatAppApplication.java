package com.boatapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point of the Boat App backend application.
 */
@SpringBootApplication
@EnableJpaAuditing
public class BoatAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoatAppApplication.class, args);
    }
}

