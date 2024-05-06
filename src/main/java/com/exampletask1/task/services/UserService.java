package com.exampletask1.task.services;

import com.exampletask1.task.dto.request.RequestUserDto;
import com.exampletask1.task.models.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    List<User> getUsers();

    Optional<User> addUser(RequestUserDto requestUserDto);

    Optional<User> findUser(Long userId);

    Optional<User> updateUser(Long userId, RequestUserDto requestUserDto);

    Optional<User> updateUserFileds(Long userId, Map<String, Object> updates);

    Optional<Long> deleteUser(Long userId);

    List<User> getUsersByBirthdayRange(LocalDate from, LocalDate to);
}
