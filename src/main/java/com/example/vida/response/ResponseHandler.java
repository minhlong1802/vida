package com.example.vida.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
    public static ResponseEntity<Object> ResponseBuilder(
            String message, HttpStatus httpStatus, Object ResponseObject
    ) {
        Map<String,Object> response = new HashMap<>(); // create response object
        response.put("message", message);   // add message
        response.put("HttpStatus", httpStatus.value()); // add http status
        response.put("data", ResponseObject); // add response object
        return new ResponseEntity<>(response, httpStatus); // return response
    }
}
