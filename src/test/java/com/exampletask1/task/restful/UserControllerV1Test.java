package com.exampletask1.task.restful;

import com.exampletask1.task.dto.request.RequestUserDto;
import com.exampletask1.task.models.User;
import com.exampletask1.task.services.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserControllerV1.class)
class UserControllerV1Test {

    @Value("${min.age}")
    private static int minAge;

    @MockBean
    private UserServiceImpl userServiceImpl;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("getAllUsersArguments")
    void getAllUsers(List<User> users, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
//        When
        when(userServiceImpl.getUsers()).thenReturn(users);
//        Then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(expectedResultMatcher);
    }


    static Stream<Arguments> getAllUsersArguments() {
        return Stream.of(
                Arguments.of(new ArrayList<>(), status().isOk()),
                Arguments.of(List.of(
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        new User(2L, "labunets@example.com", "Jane", "Smith", LocalDate.of(1985, 10, 20), "456 Elm St", "987-654-3210")

                ), status().isOk())
        );
    }

    @ParameterizedTest
    @MethodSource("getUserArguments")
    void getUser(String userIdString, User user, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
        try {
            Long userId = Long.parseLong(userIdString);
//        When
            when(userServiceImpl.findUser(userId)).thenReturn(Optional.ofNullable(user));
//        Then
            mockMvc.perform(get("/api/v1/users/" + userId))
                    .andExpect(expectedResultMatcher);
        } catch (NumberFormatException e) {
            mockMvc.perform(get("/api/v1/users/" + userIdString))
                    .andExpect(expectedResultMatcher);
        }
    }

    static Stream<Arguments> getUserArguments() {
        return Stream.of(
                Arguments.of("1", new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"), status().isOk()),
                Arguments.of("1", null, status().isNotFound()),
                Arguments.of("asd", null, status().isBadRequest())
        );
    }

    @ParameterizedTest
    @MethodSource("createUserArguments")
    void createUser(RequestUserDto requestUserDto, User user, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
//        When
        when(userServiceImpl.addUser(requestUserDto)).thenReturn(Optional.ofNullable(user));
//        Then
        mockMvc.perform(
                        post("http://localhost:8080/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestUserDto))
                )
                .andExpect(expectedResultMatcher);
    }

    static Stream<Arguments> createUserArguments() {

        Period between = Period.between(LocalDate.of(2004, 1, 21), LocalDate.now());
        int i = between.getYears() + ((minAge - 1) - between.getYears());
        LocalDate unacceptableAge = LocalDate.now().minusYears(i);

        return Stream.of(
//                всё ок
                Arguments.of(
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        status().isCreated()
                ),
//                возраст меньше 18
                Arguments.of(
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", unacceptableAge, "123 Main St", "123-456-7890"),
                        null,
                        status().isBadRequest()
                ),
//                дата рождения позже текущей даты
                Arguments.of(
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.now().plusYears(5), "123 Main St", "123-456-7890"),
                        null,
                        status().isBadRequest()
                ),
//                требуемые поля null
                Arguments.of(
                        new RequestUserDto(),
                        null,
                        status().isBadRequest()
                ),
//                требуемые поля empty
                Arguments.of(
                        new RequestUserDto("", "", "", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        null,
                        status().isBadRequest()
                ),
//                необязательные поля null
                Arguments.of(
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        status().isCreated()
                ),
//                необязательные поля empty
                Arguments.of(
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "", ""),
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "", ""),
                        status().isCreated()
                ),
//                почта невалидна
                Arguments.of(
                        new RequestUserDto("labunets@example...--com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        null,
                        status().isBadRequest()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("updateUserFieldsArguments")
    void updateUserFields(String userIdString, Map<String, Object> updates, User findedUser, User user, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
        try {
            Long userId = Long.parseLong(userIdString);
//        When
            when(userServiceImpl.findUser(userId)).thenReturn(Optional.ofNullable(findedUser));
            when(userServiceImpl.updateUserFileds(userId, updates)).thenReturn(Optional.ofNullable(user));
//        Then
            mockMvc.perform(
                            patch("http://localhost:8080/api/v1/users/" + userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updates))
                    )
                    .andExpect(expectedResultMatcher);
        } catch (NumberFormatException e) {
            mockMvc.perform(
                            patch("http://localhost:8080/api/v1/users/" + userIdString)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updates))
                    )
                    .andExpect(expectedResultMatcher);
        }
    }

    static Stream<Arguments> updateUserFieldsArguments() {
        Map<String, Object> tc1Updates = new HashMap<>();
        tc1Updates.put("email", "davyd@example.com");

        Map<String, Object> tc2Updates = new HashMap<>();
        tc2Updates.put("email", "davyd@example.com");
        tc2Updates.put("address", "");
        tc2Updates.put("phoneNumber", "");

        Map<String, Object> tc3Updates = new HashMap<>();
        tc3Updates.put("email", "davyd@example.com");
        tc3Updates.put("address", null);
        tc3Updates.put("phoneNumber", null);

        Map<String, Object> tc4Updates = new HashMap<>();
        tc4Updates.put("email", "");
        tc4Updates.put("firstName", "");
        tc4Updates.put("lastName", "");
        tc4Updates.put("birthday", "");

        Map<String, Object> tc5Updates = new HashMap<>();
        tc5Updates.put("email", null);
        tc5Updates.put("firstName", null);
        tc5Updates.put("lastName", null);
        tc5Updates.put("birthday", null);

        Map<String, Object> tc6Updates = new HashMap<>();
        tc6Updates.put("email", "labunets@example...--com");

        HashMap<Object, Object> tc7Updates = new HashMap<>();
        tc7Updates.put("birthday", LocalDate.now().plusYears(5));

        HashMap<Object, Object> tc8Updates = new HashMap<>();
        Period between = Period.between(LocalDate.of(2004, 1, 21), LocalDate.now());
        int i = between.getYears() + ((minAge - 1) - between.getYears());
        LocalDate unacceptableAge = LocalDate.now().minusYears(i);
        tc8Updates.put("birthday", unacceptableAge);

        User findedUser = new User();
        findedUser.setId(1L);
        findedUser.setEmail("labunets@gmail.com");
        findedUser.setFirstName("Davyd");
        findedUser.setLastName("Labunets");
        findedUser.setBirthday(LocalDate.of(1990, 5, 15));
        findedUser.setAddress("123 Main St");
        findedUser.setPhoneNumber("123-456-7890");

        return Stream.of(
                Arguments.of(
                        "1",
                        tc1Updates,
                        findedUser,
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        status().isOk()
                ),
                Arguments.of(
                        "1",
                        tc2Updates,
                        findedUser,
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "", ""),
                        status().isOk()
                ),
                Arguments.of(
                        "1",
                        tc3Updates,
                        findedUser,
                        new User(1L, "davyd@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        status().isOk()
                ),
                Arguments.of(
                        "1",
                        tc4Updates,
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
                Arguments.of(
                        "1",
                        tc5Updates,
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
                Arguments.of(
                        "1",
                        tc6Updates,
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
                Arguments.of(
                        "1",
                        tc7Updates,
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
                Arguments.of(
                        "1",
                        tc8Updates,
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
                Arguments.of(
                        "100",
                        tc1Updates,
                        null,
                        null,
                        status().isNotFound()
                ),
                Arguments.of(
                        "abc",
                        tc1Updates,
                        null,
                        null,
                        status().isBadRequest()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("updateUserArguments")
    void updateUser(String userIdString, RequestUserDto requestUserDto, User findedUser, User user, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
        try {
            Long userId = Long.parseLong(userIdString);
//        When
            when(userServiceImpl.findUser(userId)).thenReturn(Optional.ofNullable(findedUser));
            when(userServiceImpl.updateUser(userId, requestUserDto)).thenReturn(Optional.ofNullable(user));
//        Then
            mockMvc.perform(
                            put("http://localhost:8080/api/v1/users/" + userId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestUserDto))
                    )
                    .andExpect(expectedResultMatcher);
        } catch (NumberFormatException e) {
            mockMvc.perform(
                            put("http://localhost:8080/api/v1/users/" + userIdString)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestUserDto))
                    )
                    .andExpect(expectedResultMatcher);
        }
    }

    static Stream<Arguments> updateUserArguments() {
        Period between = Period.between(LocalDate.of(2004, 1, 21), LocalDate.now());
        int i = between.getYears() + ((minAge - 1) - between.getYears());
        LocalDate unacceptableAge = LocalDate.now().minusYears(i);

        User findedUser = new User();
        findedUser.setId(1L);
        findedUser.setEmail("labunets@gmail.com");
        findedUser.setFirstName("Davyd");
        findedUser.setLastName("Labunets");
        findedUser.setBirthday(LocalDate.of(1990, 5, 15));
        findedUser.setAddress("123 Main St");
        findedUser.setPhoneNumber("123-456-7890");

        return Stream.of(
//                всё ок
                Arguments.of(
                        "1",
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        findedUser,
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        status().isOk()
                ),
//                возраст меньше 18
                Arguments.of(
                        "1",
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", unacceptableAge, "123 Main St", "123-456-7890"),
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
//                дата рождения позже текущей даты
                Arguments.of(
                        "1",
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.now().plusYears(5), "123 Main St", "123-456-7890"),
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
//                требуемые поля null
                Arguments.of(
                        "1",
                        new RequestUserDto(),
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
//                требуемые поля empty
                Arguments.of(
                        "1",
                        new RequestUserDto("", "", "", LocalDate.of(2004, 1, 21), "123 Main St", "123-456-7890"),
                        findedUser,
                        null,
                        status().isBadRequest()
                ),
//                необязательные поля null
                Arguments.of(
                        "1",
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        findedUser,
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        status().isOk()
                ),
//                необязательные поля empty
                Arguments.of(
                        "1",
                        new RequestUserDto("labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "", ""),
                        findedUser,
                        new User(1L, "labunets@example.com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), "", ""),
                        status().isOk()
                ),
//                почта невалидна
                Arguments.of(
                        "1",
                        new RequestUserDto("labunets@example...--com", "Davyd", "Labunets", LocalDate.of(2004, 1, 21), null, null),
                        findedUser,
                        null,
                        status().isBadRequest()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("deleteUserArguments")
    void deleteUser(String userIdString, Long idDeletedUser, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
        try {
            Long userId = Long.parseLong(userIdString);
//        When
            when(userServiceImpl.deleteUser(userId)).thenReturn(Optional.ofNullable(idDeletedUser));
//        Then
            mockMvc.perform(delete("http://localhost:8080/api/v1/users/" + userId))
                    .andExpect(expectedResultMatcher);
        } catch (NumberFormatException e) {
            mockMvc.perform(delete("http://localhost:8080/api/v1/users/" + userIdString))
                    .andExpect(expectedResultMatcher);
        }
    }

    static Stream<Arguments> deleteUserArguments() {
        return Stream.of(
                Arguments.of("1", 1L, status().isOk()),
                Arguments.of("1", null, status().isNotFound())
        );
    }

    @ParameterizedTest
    @MethodSource("getUsersByBirthdaysArguments")
    void getUsersByBirthdays(String from, String to, List<User> users, ResultMatcher expectedResultMatcher) throws Exception {
//        Given
        try {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
//        When
            when(userServiceImpl.getUsersByBirthdayRange(fromDate, toDate)).thenReturn(users);
//        Then
            mockMvc.perform(
                    get("http://localhost:8080/api/v1/users/birthdays")
                            .param("from", fromDate.toString())
                            .param("to", toDate.toString())
            ).andExpect(expectedResultMatcher);
        } catch (DateTimeParseException e) {
            mockMvc.perform(
                    get("http://localhost:8080/api/v1/users/birthdays")
                            .param("from", from)
                            .param("to", to)
            ).andExpect(expectedResultMatcher);
        }
    }

    static Stream<Arguments> getUsersByBirthdaysArguments() {
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

        return Stream.of(
                Arguments.of(user2.getBirthday().toString(), user3.getBirthday().toString(), List.of(user1, user2, user3), status().isOk()),
                Arguments.of(user2.getBirthday().toString(), user1.getBirthday().toString(), List.of(user1, user2), status().isOk()),
                Arguments.of(user2.getBirthday().toString(), user2.getBirthday().toString(), null, status().isBadRequest()),
                Arguments.of(user1.getBirthday().toString(), user2.getBirthday().toString(), null, status().isBadRequest()),
                Arguments.of("", "", null, status().isBadRequest()),
                Arguments.of(user1.getBirthday().toString(), "1995-3-25", null, status().isBadRequest())
        );
    }
}