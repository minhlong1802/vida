package com.example.vida.controller;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.entity.Appointment;
import com.example.vida.enums.RecurrencePattern;
import com.example.vida.exception.ConflictException;
import com.example.vida.service.AppointmentService;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
                    .toList();

            return APIResponse.responseBuilder(
                    null,
                    errors.get(0), // Get first error message
                    HttpStatus.BAD_REQUEST
            );
        }
        if(createAppointmentDto.getWeeklyDay()==null&&createAppointmentDto.getRecurrencePattern()==RecurrencePattern.Weekly){
            return APIResponse.responseBuilder(
                    null,
                    "weeklyDays is necessary for weekly meeting",
                    HttpStatus.BAD_REQUEST
            );
        }
        try {
            Appointment appointment = appointmentService.createAppointment(createAppointmentDto);
            return APIResponse.responseBuilder(
                    appointment,
                    "Appointment created successfully",
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid appointment data: {}", e.getMessage());
            return APIResponse.responseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (ConflictException e) {
            log.error("Appointment conflict: {}", e.getMessage());
            return APIResponse.responseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Error creating appointment", e);
            return APIResponse.responseBuilder(
                    null,
                    "Error creating appointment",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @GetMapping()
    public ResponseEntity<Object> searchDepartments(@RequestParam String searchText,
                                                    @RequestParam @Nullable Integer roomId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam List<Integer> userIds) {
        try {
            Map<String, Object> mapAppointment = appointmentService.searchAppointmentByTitle(searchText, roomId, page, size, userIds);
            return APIResponse.responseBuilder(mapAppointment, null, HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
