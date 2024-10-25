package com.example.vida.dto.response;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class APIResponse {
    public static ResponseEntity<Object> ResponseBuilder(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("message", message);
        response.put("httpStatus", status.value());

        return new ResponseEntity<>(response, status);
    }
}
