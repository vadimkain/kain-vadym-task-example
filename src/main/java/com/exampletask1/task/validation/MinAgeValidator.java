package com.exampletask1.task.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.Period;

public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    @Value("${min.age}")
    private int minAge;

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {

        if (localDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        Period period = Period.between(localDate, today);

        return period.getYears() >= minAge;
    }
}
