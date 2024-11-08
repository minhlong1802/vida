package com.example.vida.annotation;

import com.example.vida.service.impl.DateValidatorImpl;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateValidatorImpl.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDate {
    String message() default "Invalid date format or date does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}