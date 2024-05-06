package com.exampletask1.task.advice;

import com.exampletask1.task.dto.response.ResponseAdviceDto;
import com.exampletask1.task.exception.UpdateUserFieldsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class UserControllerAdvice {

    private static final Logger log = LogManager.getLogger(UserControllerAdvice.class);

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseAdviceDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ResponseAdviceDto responseAdviceDto = new ResponseAdviceDto();
        responseAdviceDto.setStatus(HttpStatus.BAD_REQUEST.toString());

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            ResponseAdviceDto.ResponseErrorDto responseErrorDto = new ResponseAdviceDto.ResponseErrorDto();
            responseErrorDto.setCode("MethodArgumentNotValidException");
            responseErrorDto.setMessage(fieldError.getField() + " " + fieldError.getDefaultMessage());
            responseAdviceDto.getErrors().add(responseErrorDto);
        }


        log.error(responseAdviceDto);

        return ResponseEntity.badRequest().body(responseAdviceDto);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseAdviceDto> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return typicalResponseFormation("MissingServletRequestParameterException", ex.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseAdviceDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return typicalResponseFormation("MethodArgumentTypeMismatchException", ex.getMessage());
    }

    @ExceptionHandler(value = UpdateUserFieldsException.class)
    public ResponseEntity<ResponseAdviceDto> handleUpdateUserFieldsException(UpdateUserFieldsException ex) {

        ResponseAdviceDto responseAdviceDto = ex.getResponseAdviceDto();
        responseAdviceDto.setStatus(HttpStatus.BAD_REQUEST.toString());

        return ResponseEntity.badRequest().body(responseAdviceDto);
    }

    private ResponseEntity<ResponseAdviceDto> typicalResponseFormation(String code, String message) {
        ResponseAdviceDto responseAdviceDto = new ResponseAdviceDto();
        ResponseAdviceDto.ResponseErrorDto responseErrorDto = new ResponseAdviceDto.ResponseErrorDto();

        responseAdviceDto.setStatus(HttpStatus.BAD_REQUEST.toString());
        responseErrorDto.setCode(code);
        responseErrorDto.setMessage(message);
        responseAdviceDto.getErrors().add(responseErrorDto);
        log.error(responseAdviceDto);

        return ResponseEntity.badRequest().body(responseAdviceDto);
    }
}
