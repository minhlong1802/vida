package com.example.vida.service;

import com.example.vida.dto.request.RequestAppointmentDto;
import com.example.vida.entity.Appointment;

import java.time.LocalDate;
import java.util.Map;

public interface AppointmentService {
    Appointment createAppointment(RequestAppointmentDto requestAppointmentDto);
    Map<String, Object> searchAppointmentByTitle(String searchText, Integer roomId, int page, int size, Integer userId);
    Map<String, String> validateAppointmentData(RequestAppointmentDto requestAppointmentDto);
    Appointment updateAppointment(Integer id, RequestAppointmentDto requestAppointmentDto);
    Appointment getAppointmentById(Integer id);
    void deleteAppointment(Integer id);
}
