package com.example.vida.repository;

import com.example.vida.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByDateAndStartTimeGreaterThanEqual(LocalDate date, LocalTime startTime);
}
