package com.exampletask1.task.services;

import jakarta.validation.Validator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class UserServiceImplConfiguration {
    @Bean
    public UserService userService(Validator validator) {
        return new UserServiceImpl(validator);
    }
}
