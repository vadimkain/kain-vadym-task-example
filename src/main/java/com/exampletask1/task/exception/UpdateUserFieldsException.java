package com.exampletask1.task.exception;

import com.exampletask1.task.dto.response.ResponseAdviceDto;
import lombok.Getter;

public class UpdateUserFieldsException extends RuntimeException {

    @Getter
    private final ResponseAdviceDto responseAdviceDto;

    public UpdateUserFieldsException(ResponseAdviceDto responseAdviceDto) {
        this.responseAdviceDto = responseAdviceDto;
    }
}
