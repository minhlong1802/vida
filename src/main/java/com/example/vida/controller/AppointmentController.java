package com.example.vida.controller;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.entity.Appointment;
import com.example.vida.exception.ConflictException;
import com.example.vida.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody CreateAppointmentDto createAppointmentDto) {
        try {
            Appointment createdAppointment = appointmentService.createAppointment(createAppointmentDto);
            return new ResponseEntity<>(createdAppointment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("Invalid appointment data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ConflictException e) {
            log.error("Appointment conflict: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("Error creating appointment", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating appointment");
        }
    }
}
