package com.example.vida.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.Map;

@Getter
public class ConflictException extends RuntimeException {
    private final Map<LocalDate, String> conflicts;

    public ConflictException(Map<LocalDate, String> conflicts) {
        super("Appointments have conflicts on dates: " + conflicts.keySet());
        this.conflicts = conflicts;
    }

}
