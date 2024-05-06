package com.exampletask1.task.dto.response;

import lombok.Data;

@Data
public class ResponseUserDeletedDto {
    private Long userId;
    private Boolean isDeleted;
}
