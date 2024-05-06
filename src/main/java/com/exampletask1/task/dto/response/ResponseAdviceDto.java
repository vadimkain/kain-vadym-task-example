package com.exampletask1.task.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseAdviceDto {

    private String status;
    List<ResponseErrorDto> errors = new ArrayList<>();

    @Data
    public static class ResponseErrorDto {
        private String code;
        private String message;
    }
}
