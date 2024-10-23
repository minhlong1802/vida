package com.example.vida.service;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.entity.Appointment;

public interface AppointmentService {
    Appointment createAppointment(CreateAppointmentDto createAppointmentDto);
}
