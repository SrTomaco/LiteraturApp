package com.literaturaapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.literaturaapp.service.ConsoleService;

@SpringBootApplication
public class LiteraturaApp {
    public static void main(String[] args) {
        SpringApplication.run(LiteraturaApp.class, args);
    }

    @Bean
    public CommandLineRunner runner(ConsoleService consoleService) {
        return args -> consoleService.start();
    }
}
