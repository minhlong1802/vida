package com.example.vida.service.impl;

import com.example.vida.annotation.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateValidatorImpl implements ConstraintValidator<ValidDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // Let @NotNull handle null validation
        }

        try {
            // Use STRICT resolver to ensure dates like "2024-04-31" are invalid
            String dateString = date.format(DateTimeFormatter.ISO_DATE);
            LocalDate.parse(dateString,
                    DateTimeFormatter.ISO_DATE.withResolverStyle(ResolverStyle.STRICT));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}