package com.example.vida.exception;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.response.APIResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return APIResponse.responseBuilder(
                null,
                errorMessage,
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return APIResponse.responseBuilder(
                null,
                "Invalid input format. Please check the request body",
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<Object> handleUserValidationException(UserValidationException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException e) {
        return APIResponse.responseBuilder(
                null,
                e.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }
    @ExceptionHandler(ImportUserValidationException.class)
    public ResponseEntity<Map<String, String>> handleImportValidationExceptions(ImportUserValidationException ex,BindingResult bindingResult) {
        Map<String, String> validationErrors = new HashMap<>();
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error;
                    String fieldName = fieldError.getField();
                    String errorMessage = fieldError.getDefaultMessage();
                    int rowNumber = ((CreateUserDto) fieldError.getRejectedValue()).getRowNumber();
                    validationErrors.put("row " + rowNumber, fieldName + ": " + errorMessage);
                }
            });
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrors);
    }
}
