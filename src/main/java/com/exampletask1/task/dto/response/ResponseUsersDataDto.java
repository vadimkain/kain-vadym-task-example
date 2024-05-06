package com.exampletask1.task.dto.response;

import com.exampletask1.task.models.User;
import lombok.Data;
import org.springframework.hateoas.EntityModel;

import java.util.List;

@Data
public class ResponseUsersDataDto {
    private List<EntityModel<User>> users;
}
