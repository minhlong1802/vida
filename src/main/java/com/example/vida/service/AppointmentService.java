package com.example.vida.service;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    Appointment createAppointment(CreateAppointmentDto createAppointmentDto);
    Map<String, Object> searchAppointmentByTitle(String searchText, Integer roomId, int page, int size, Integer userId);
    Map<String, String> validateAppointmentData(CreateAppointmentDto createAppointmentDto);
}
