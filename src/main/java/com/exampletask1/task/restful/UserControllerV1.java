package com.exampletask1.task.restful;

import com.exampletask1.task.dto.request.RequestUserDto;
import com.exampletask1.task.dto.response.ResponseUserDataDto;
import com.exampletask1.task.dto.response.ResponseUserDeletedDto;
import com.exampletask1.task.dto.response.ResponseUsersDataDto;
import com.exampletask1.task.models.User;
import com.exampletask1.task.services.UserServiceImpl;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    private static final Logger log = LogManager.getLogger(UserControllerV1.class);

    private final UserServiceImpl userServiceImpl;

    public UserControllerV1(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("")
    public ResponseEntity<ResponseUsersDataDto> getAllUsers() {
        List<User> users = userServiceImpl.getUsers();
        ResponseUsersDataDto responseUsersDataDto = new ResponseUsersDataDto();

        List<EntityModel<User>> userModels = new ArrayList<>();
        for (User user : users) {
            userModels.add(
                    EntityModel.of(user)
                            .add(linkTo(methodOn(UserControllerV1.class).getUser(user.getId())).withSelfRel())
            );
        }

        responseUsersDataDto.setUsers(userModels);

        return ResponseEntity
                .ok()
                .body(responseUsersDataDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<EntityModel<ResponseUserDataDto>> getUser(@PathVariable Long userId) {
        Optional<User> userOptional = userServiceImpl.findUser(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            ResponseUserDataDto responseUserDataDto = new ResponseUserDataDto();
            responseUserDataDto.setUser(user);

            return ResponseEntity
                    .ok()
                    .body(
                            EntityModel.of(responseUserDataDto)
                                    .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                    );
        }

        return ResponseEntity
                .notFound()
                .build();
    }

    @PostMapping("")
    public ResponseEntity<EntityModel<ResponseUserDataDto>> createUser(@RequestBody @Valid RequestUserDto requestUserDto) {
        Optional<User> userOptional = userServiceImpl.addUser(requestUserDto);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            ResponseUserDataDto responseUserDataDto = new ResponseUserDataDto();
            responseUserDataDto.setUser(user);

            return ResponseEntity
                    .created(URI.create("/api/v1/users/" + user.getId()))
                    .body(
                            EntityModel.of(responseUserDataDto)
                                    .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                                    .add(linkTo(methodOn(UserControllerV1.class).getUser(user.getId())).withSelfRel())
                    );
        }

        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<EntityModel<ResponseUserDataDto>> updateUserFields(@PathVariable Long userId, @RequestBody Map<String, Object> updates) {

        if (userServiceImpl.findUser(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> userOptional = userServiceImpl.updateUserFileds(userId, updates);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            ResponseUserDataDto responseUserDataDto = new ResponseUserDataDto();
            responseUserDataDto.setUser(user);

            return ResponseEntity.ok()
                    .body(
                            EntityModel.of(responseUserDataDto)
                                    .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                                    .add(linkTo(methodOn(UserControllerV1.class).getUser(user.getId())).withSelfRel())
                    );
        }

        return ResponseEntity.badRequest().body(
                EntityModel.of(new ResponseUserDataDto())
                        .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                        .add(linkTo(methodOn(UserControllerV1.class).getUser(userId)).withSelfRel())
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<EntityModel<ResponseUserDataDto>> updateUser(@PathVariable Long userId, @Valid @RequestBody RequestUserDto requestUserDto) {

        if (userServiceImpl.findUser(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<User> userOptional = userServiceImpl.updateUser(userId, requestUserDto);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            ResponseUserDataDto responseUserDataDto = new ResponseUserDataDto();
            responseUserDataDto.setUser(user);

            return ResponseEntity.ok()
                    .body(
                            EntityModel.of(responseUserDataDto)
                                    .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                                    .add(linkTo(methodOn(UserControllerV1.class).getUser(user.getId())).withSelfRel())
                    );
        }

        return ResponseEntity.badRequest().body(
                EntityModel.of(new ResponseUserDataDto())
                        .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                        .add(linkTo(methodOn(UserControllerV1.class).getUser(userId)).withSelfRel())
        );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<EntityModel<ResponseUserDeletedDto>> deleteUser(@PathVariable Long userId) {
        Optional<Long> deletedUserId = userServiceImpl.deleteUser(userId);

        if (deletedUserId.isPresent()) {
            ResponseUserDeletedDto responseUserDeletedDto = new ResponseUserDeletedDto();
            responseUserDeletedDto.setUserId(deletedUserId.get());
            responseUserDeletedDto.setIsDeleted(true);

            return ResponseEntity.ok()
                    .body(
                            EntityModel.of(responseUserDeletedDto)
                                    .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                    );
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/birthdays")
    public ResponseEntity<EntityModel<ResponseUsersDataDto>> getUsersByBirthdays(
            @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to
    ) {
        if (from.isBefore(to)) {
            List<User> usersByBirthdayRange = userServiceImpl.getUsersByBirthdayRange(from, to);

            ResponseUsersDataDto responseUsersDataDto = new ResponseUsersDataDto();

            List<EntityModel<User>> userModels = new ArrayList<>();
            for (User user : usersByBirthdayRange) {
                userModels.add(
                        EntityModel.of(user)
                                .add(linkTo(methodOn(UserControllerV1.class).getUser(user.getId())).withSelfRel())
                );
            }

            responseUsersDataDto.setUsers(userModels);

            return ResponseEntity
                    .ok()
                    .body(
                            EntityModel.of(responseUsersDataDto)
                                    .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
                    );
        } else {
            return ResponseEntity.badRequest().body(
                    EntityModel.of(new ResponseUsersDataDto())
                            .add(linkTo(methodOn(UserControllerV1.class).getAllUsers()).withSelfRel())
            );
        }
    }
}
