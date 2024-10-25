package com.example.vida.controller;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.entity.Appointment;
import com.example.vida.enums.RecurrencePattern;
import com.example.vida.exception.AppointmentValidationException;
import com.example.vida.exception.ConflictException;
import com.example.vida.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Object> createAppointment(@Valid @RequestBody CreateAppointmentDto createAppointmentDto,
                                                    BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .map(error ->  error.getDefaultMessage())
                    .collect(Collectors.toList());

            return APIResponse.ResponseBuilder(
                    null,
                    errors.get(0), // Get first error message
                    HttpStatus.BAD_REQUEST
            );
        }
        try {
            Appointment appointment = appointmentService.createAppointment(createAppointmentDto);
            return APIResponse.ResponseBuilder(
                    appointment,
                    "Appointment created successfully",
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid appointment data: {}", e.getMessage());
            return APIResponse.ResponseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (ConflictException e) {
            log.error("Appointment conflict: {}", e.getMessage());
            return APIResponse.ResponseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error creating appointment", e);
            return APIResponse.ResponseBuilder(
                    null,
                    "Error creating appointment",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
