package com.streamvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StreamVaultApplication {
    public static void main(String[] args) {
        SpringApplication.run(StreamVaultApplication.class, args);
    }
}