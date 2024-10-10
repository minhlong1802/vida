package com.example.vida.exception;

public class AppointmentValidationException extends RuntimeException {
    public AppointmentValidationException(String message) {
        super(message);
    }
}
