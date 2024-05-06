package com.exampletask1.task.services;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceImplConfiguration.class)
class UserServiceImplTest {

    @MockBean
    private Validator validator;

    @Autowired
    private UserService userService;

    @Test
    void getUsers() {
    }

    @Test
    void addUser() {
    }

    @Test
    void findUser() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void updateUserFileds() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void getUsersByBirthdayRange() {
    }
}