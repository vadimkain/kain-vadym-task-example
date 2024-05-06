package com.exampletask1.task.services;

import com.exampletask1.task.dto.request.RequestUserDto;
import com.exampletask1.task.dto.response.ResponseAdviceDto;
import com.exampletask1.task.exception.UpdateUserFieldsException;
import com.exampletask1.task.models.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserServiceImpl implements UserService {

    private static ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    private final Validator validator;

    public UserServiceImpl(Validator validator) {
        this.validator = validator;

        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("labunets@gmail.com");
        user1.setFirstName("Davyd");
        user1.setLastName("Labunets");
        user1.setBirthday(LocalDate.of(1990, 5, 15));
        user1.setAddress("123 Main St");
        user1.setPhoneNumber("123-456-7890");

        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("smith@gmail.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setBirthday(LocalDate.of(1985, 10, 20));
        user2.setAddress("456 Elm St");
        user2.setPhoneNumber("987-654-3210");

        User user3 = new User();
        user3.setId(3L);
        user3.setEmail("johnson@gmail.com");
        user3.setFirstName("Alice");
        user3.setLastName("Johnson");
        user3.setBirthday(LocalDate.of(1995, 3, 25));
        user3.setAddress("789 Oak St");
        user3.setPhoneNumber("555-123-4567");

        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        users.put(user3.getId(), user3);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> addUser(RequestUserDto requestUserDto) {
        User user = new User();
        user.setId(users.size() + 1L);
        return fillUser(requestUserDto, user);
    }

    private Optional<User> fillUser(RequestUserDto requestUserDto, User user) {
        user.setEmail(requestUserDto.getEmail());
        user.setFirstName(requestUserDto.getFirstName());
        user.setLastName(requestUserDto.getLastName());
        user.setBirthday(requestUserDto.getBirthday());
        user.setAddress(requestUserDto.getAddress());
        user.setPhoneNumber(requestUserDto.getPhoneNumber());

        users.put(user.getId(), user);

        return Optional.of(user);
    }

    @Override
    public Optional<User> findUser(Long userId) {

        User user = users.get(userId);

        return Optional.ofNullable(user);

    }

    @Override
    public Optional<User> updateUser(Long userId, RequestUserDto requestUserDto) {
        Optional<User> userOptional = Optional.ofNullable(users.get(userId));

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            return fillUser(requestUserDto, user);
        }

        return Optional.empty();
    }


    @Override
    public Optional<User> updateUserFileds(Long userId, Map<String, Object> updates) {
        Optional<User> userOptional = Optional.ofNullable(users.get(userId));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            User userSnapshot = new User();
            userSnapshot.setId(user.getId());
            userSnapshot.setEmail(user.getEmail());
            userSnapshot.setFirstName(user.getFirstName());
            userSnapshot.setLastName(user.getLastName());
            userSnapshot.setBirthday(user.getBirthday());
            userSnapshot.setAddress(user.getAddress());
            userSnapshot.setPhoneNumber(user.getPhoneNumber());

            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                Field field = ReflectionUtils.findField(User.class, entry.getKey());
                field.setAccessible(true);
                if (!entry.getKey().equals("id")) {
                    try {
                        ReflectionUtils.setField(field, user, entry.getValue());
                    } catch (IllegalArgumentException e) {

                        ResponseAdviceDto responseAdviceDto = new ResponseAdviceDto();
                        ResponseAdviceDto.ResponseErrorDto responseErrorDto = new ResponseAdviceDto.ResponseErrorDto();

                        responseAdviceDto.setStatus(HttpStatus.BAD_REQUEST.toString());
                        responseErrorDto.setCode("IllegalArgumentException");
                        responseErrorDto.setMessage(e.getMessage());
                        responseAdviceDto.getErrors().add(responseErrorDto);

                        users.put(user.getId(), userSnapshot);
                        throw new UpdateUserFieldsException(responseAdviceDto);
                    }
                }
            }

            Set<ConstraintViolation<User>> validated = validator.validate(user);

            if (validated.isEmpty()) {
                return Optional.of(user);

            } else {

                List<ResponseAdviceDto.ResponseErrorDto> responseErrorList = new ArrayList<>();
                for (ConstraintViolation<User> violation : validator.validate(user)) {
                    ResponseAdviceDto.ResponseErrorDto responseErrorDto = new ResponseAdviceDto.ResponseErrorDto();
                    responseErrorDto.setCode("Validation");
                    responseErrorDto.setMessage(violation.getMessage());
                    responseErrorList.add(responseErrorDto);
                }

                users.put(user.getId(), userSnapshot);

                ResponseAdviceDto responseAdviceDto = new ResponseAdviceDto();

                responseAdviceDto.setErrors(responseErrorList);

                throw new UpdateUserFieldsException(responseAdviceDto);
            }
        }

        ResponseAdviceDto responseAdviceDto = new ResponseAdviceDto();
        ResponseAdviceDto.ResponseErrorDto responseErrorDto = new ResponseAdviceDto.ResponseErrorDto();
        responseErrorDto.setCode("");
        responseErrorDto.setMessage("User does not exist");
        responseAdviceDto.getErrors().add(responseErrorDto);

        throw new UpdateUserFieldsException(responseAdviceDto);
    }

    @Override
    public Optional<Long> deleteUser(Long userId) {
        Optional<User> userOptional = Optional.ofNullable(users.get(userId));
        if (userOptional.isPresent()) {
            users.remove(userId);
            return Optional.of(userId);
        }

        return Optional.empty();
    }

    @Override
    public List<User> getUsersByBirthdayRange(LocalDate from, LocalDate to) {
        List<User> resultUsers = new ArrayList<>();

        for (User user : users.values()) {
            LocalDate birthday = user.getBirthday();
            if (birthday.isAfter(from) && birthday.isBefore(to) || birthday.isEqual(from) || birthday.isEqual(to)) {
                resultUsers.add(user);
            }
        }

        return resultUsers;
    }
}
