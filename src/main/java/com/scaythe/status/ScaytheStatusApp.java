package com.scaythe.status;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import oshi.SystemInfo;

@SpringBootApplication
public class ScaytheStatusApp {

    public static void main(String[] args) {
        SpringApplication.run(ScaytheStatusApp.class, args);

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
        }
    }

    @Bean
    public SystemInfo systemInfo() {
        return new SystemInfo();
    }
}
