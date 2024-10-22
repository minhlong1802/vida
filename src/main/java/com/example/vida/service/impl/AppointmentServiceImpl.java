package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.entity.Appointment;
import com.example.vida.entity.Room;
import com.example.vida.entity.User;
import com.example.vida.enums.RecurrencePattern;
import com.example.vida.exception.ConflictException;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.repository.AppointmentRepository;
import com.example.vida.repository.RoomRepository;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.AppointmentService;
import com.example.vida.utils.UserContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    public Appointment createAppointment(CreateAppointmentDto createAppointmentDto) {
        validateAppointmentData(createAppointmentDto);

        // Tạo appointment mới
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(createAppointmentDto,appointment);
        if (createAppointmentDto.getRoomId() != null) {
            Room room = roomRepository.findById(createAppointmentDto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            appointment.setRoom(room);
        } else {
            appointment.setRoom(null);
        }
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.setCreatorId(UserContext.getUser().getUserId());
        appointment.setCreatorName(UserContext.getUser().getUsername());
        appointment.setUpdatorId(UserContext.getUser().getUserId());
        appointment.setUpdatorName(UserContext.getUser().getUsername());

        // Thêm users vào appointment
        if (createAppointmentDto.getUserIds() != null && !createAppointmentDto.getUserIds().isEmpty()) {
            Set<User> users = createAppointmentDto.getUserIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)))
                    .collect(Collectors.toSet());
            appointment.setUsers(users);
        }

        // Lưu appointment
        appointment = appointmentRepository.save(appointment);
//        List<Appointment> appointments = new ArrayList<>();
//        appointmentRepository.saveAll(appointments);
        log.info("Created new appointment with id: {}", appointment.getId());

        return appointment;
    }

    private void validateAppointmentData(CreateAppointmentDto appointmentDto) {
        // 1. Time validations
        LocalDate currentDate = LocalDate.now();

        // Check if appointment date is in the past
        if (appointmentDto.getDate().isBefore(currentDate)) {
            throw new IllegalArgumentException("Appointment date cannot be in the past");
        }

        // Check if end time is before start time
        if (appointmentDto.getEndTime().isBefore(appointmentDto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check recurrence end date if present
        if (appointmentDto.getRecurrencePattern() != RecurrencePattern.Only
                && appointmentDto.getRecurrenceEndDate() != null) {
            if (appointmentDto.getRecurrenceEndDate().isBefore(appointmentDto.getDate())) {
                throw new IllegalArgumentException("Recurrence end date must be after appointment date");
            }
        }

        // 2. Conflict validations
        List<Appointment> existingAppointments = appointmentRepository
                .findByDateAndStartTimeGreaterThanEqual(
                        appointmentDto.getDate(),
                        appointmentDto.getStartTime()
                );

        // Check for time overlaps with existing appointments
        boolean hasConflict = existingAppointments.stream()
                .anyMatch(existing -> isTimeOverlap(
                        appointmentDto.getStartTime(),
                        appointmentDto.getEndTime(),
                        existing.getStartTime(),
                        existing.getEndTime()
                ));

        if (hasConflict) {
            throw new ConflictException("Appointment time conflicts with existing appointment");
        }
    }

    private boolean isTimeOverlap(
            LocalTime start1, LocalTime end1,
            LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}