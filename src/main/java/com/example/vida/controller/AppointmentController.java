package com.example.vida.controller;

import com.example.vida.dto.request.RequestAppointmentDto;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.entity.Appointment;
import com.example.vida.exception.AppointmentNotFoundException;
import com.example.vida.service.AppointmentService;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Object> createAppointment(
            @Valid @RequestBody RequestAppointmentDto requestAppointmentDto,
            BindingResult bindingResult) {

        // Thu thập tất cả các lỗi validation
        Map<String, String> errors = new HashMap<>();

        // 1. Thêm các lỗi từ @Valid annotation
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }

        // 2. Thêm các lỗi từ business validation
        try {
            Map<String, String> businessErrors = appointmentService.validateAppointmentData(requestAppointmentDto);
            errors.putAll(businessErrors);
        } catch (Exception e) {
            log.error("Error during business validation", e);
            return APIResponse.responseBuilder(
                    null,
                    "Internal validation error",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // Nếu có bất kỳ lỗi nào, trả về tất cả các lỗi
        if (!errors.isEmpty()) {
            return APIResponse.responseBuilder(
                    errors,  // Trả về map chứa tất cả các lỗi trong data
                    "Validation failed",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Xử lý tạo appointment nếu không có lỗi
        try {
            Appointment appointment = appointmentService.createAppointment(requestAppointmentDto);
            return APIResponse.responseBuilder(
                    appointment,
                    "Appointment created successfully",
                    HttpStatus.CREATED
            );
        }  catch (Exception e) {
            log.error("Error creating appointment", e);
            return APIResponse.responseBuilder(
                    null,
                    "An unexpected error occurred",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping()
    public ResponseEntity<Object> searchAppointments(@RequestParam @Nullable String searchText,
                                                    @RequestParam @Nullable Integer roomId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam @Nullable Integer userId) {
        try {
            if(page<=0&&size<=0) {
                page = 1;
                size = 1;
            }
            Map<String, Object> mapAppointment = appointmentService.searchAppointmentByTitle(searchText, roomId, page, size, userId);
            return APIResponse.responseBuilder(mapAppointment, null, HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAppointment(@PathVariable Integer id, @Valid @RequestBody RequestAppointmentDto requestAppointmentDto,BindingResult bindingResult){
        // Thu thập tất cả các lỗi validation
        Map<String, String> errors = new HashMap<>();

        // 1. Thêm các lỗi từ @Valid annotation
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }

        // 2. Thêm các lỗi từ business validation
        try {
            Map<String, String> businessErrors = appointmentService.validateAppointmentData(requestAppointmentDto);
            errors.putAll(businessErrors);
        } catch (Exception e) {
            log.error("Error during business validation", e);
            return APIResponse.responseBuilder(
                    null,
                    "Internal validation error",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // Nếu có bất kỳ lỗi nào, trả về tất cả các lỗi
        if (!errors.isEmpty()) {
            return APIResponse.responseBuilder(
                    errors,  // Trả về map chứa tất cả các lỗi trong data
                    "Validation failed",
                    HttpStatus.BAD_REQUEST
            );
        }
        try{
            Appointment appointment = appointmentService.updateAppointment(id, requestAppointmentDto);
            return APIResponse.responseBuilder(
                    appointment,
                    "Appointment update successfully",
                    HttpStatus.OK
            );
        } catch (AppointmentNotFoundException e) {
            log.warn("Appointment not found with id: {}", id, e);
            return APIResponse.responseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Unexpected error during appointment update", e);
            return APIResponse.responseBuilder(
                    null,
                    "An unexpected error occurred while updating the appointment",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @DeleteMapping()
    public ResponseEntity<Object> deleteAppointments(@RequestBody List<Integer> ids) {
        try {
            appointmentService.deleteAppointments(ids);
            return APIResponse.responseBuilder(
                    null,
                    "Appointments deleted successfully",
                    HttpStatus.OK
            );
        } catch (AppointmentNotFoundException e) {
            return APIResponse.responseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> detailAppointment(@PathVariable Integer id){
        try{
            Appointment appointment = appointmentService.getAppointmentById(id);
            return APIResponse.responseBuilder(
                    appointment,
                    "Appointment with id="+id+" return successfully",
                    HttpStatus.OK
            );
        } catch (AppointmentNotFoundException e) {
            return APIResponse.responseBuilder(
                    null,
                    "Appointment with id = "+id+" not found",
                    HttpStatus.NOT_FOUND
            );
        }

    }
}
