package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.entity.Appointment;
import com.example.vida.entity.Room;
import com.example.vida.entity.User;
import com.example.vida.enums.RecurrencePattern;
import com.example.vida.exception.AppointmentValidationException;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
        // Validate room availability
        validateRoomAvailability(createAppointmentDto);

        List<Appointment> appointments = new ArrayList<>();

        // Create base appointment
        Appointment baseAppointment = createBaseAppointment(createAppointmentDto);

        // Handle recurring appointments
        if (baseAppointment.getRecurrencePattern() != null) {
            switch (baseAppointment.getRecurrencePattern()) {
                case Daily:
                    appointments.addAll(createDailyAppointments(baseAppointment));
                    break;
                case Weekly:
                    appointments.addAll(createWeeklyAppointments(baseAppointment, createAppointmentDto.getWeeklyDay()));
                    break;
                case Only:
                    appointments.add(baseAppointment);
                    break;
            }
        } else {
            appointments.add(baseAppointment);
        }

        // Save all appointments
        appointmentRepository.saveAll(appointments);

        return baseAppointment; // Return the original appointment
    }

    private Appointment createBaseAppointment(CreateAppointmentDto createAppointmentDto) {
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(createAppointmentDto, appointment);

        // Set room
        if (createAppointmentDto.getRoomId() != null) {
            Room room = roomRepository.findById(createAppointmentDto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            appointment.setRoom(room);
        }

        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointment.setCreatorId(UserContext.getUser().getUserId());
        appointment.setCreatorName(UserContext.getUser().getUsername());
        appointment.setUpdatorId(UserContext.getUser().getUserId());
        appointment.setUpdatorName(UserContext.getUser().getUsername());

        // Set users
        if (createAppointmentDto.getUserIds() != null && !createAppointmentDto.getUserIds().isEmpty()) {
            Set<User> users = createAppointmentDto.getUserIds().stream()
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)))
                    .collect(Collectors.toSet());
            appointment.setUsers(users);
        }

        return appointment;
    }

    private List<Appointment> createDailyAppointments(Appointment baseAppointment) {
        List<Appointment> appointments = new ArrayList<>();
        LocalDate startDate = baseAppointment.getDate();
        LocalDate endDate = baseAppointment.getRecurrenceEndDate();

        while (!startDate.isAfter(endDate)) {
            Appointment newAppointment = cloneAppointment(baseAppointment);
            newAppointment.setDate(startDate);
            newAppointment.setRecurrencePattern(RecurrencePattern.Only);
            appointments.add(newAppointment);
            startDate = startDate.plusDays(1);
        }

        return appointments;
    }

    private List<Appointment> createWeeklyAppointments(Appointment baseAppointment, List<DayOfWeek> weeklyDays) {
        List<Appointment> appointments = new ArrayList<>();
        LocalDate startDate = baseAppointment.getDate();
        LocalDate endDate = baseAppointment.getRecurrenceEndDate();

        while (!startDate.isAfter(endDate)) {
            if (weeklyDays.contains(startDate.getDayOfWeek())) {
                Appointment newAppointment = cloneAppointment(baseAppointment);
                newAppointment.setDate(startDate);
                newAppointment.setRecurrencePattern(RecurrencePattern.Only);
                appointments.add(newAppointment);
            }
            startDate = startDate.plusDays(1);
        }

        return appointments;
    }

    private Appointment cloneAppointment(Appointment original) {
        Appointment clone = new Appointment();
        BeanUtils.copyProperties(original, clone);
        clone.setId(null); // Ensure new ID is generated
        clone.setCreatedAt(LocalDateTime.now());
        clone.setUpdatedAt(LocalDateTime.now());

        // Clone relationships
        if (original.getRoom() != null) {
            clone.setRoom(original.getRoom());
        }
        if (original.getUsers() != null) {
            clone.setUsers(new HashSet<>(original.getUsers()));
        }

        return clone;
    }

    private void validateAppointmentData(CreateAppointmentDto appointmentDto) {
        // 1. Time validations
        LocalDate currentDate = LocalDate.now();
        List<String> errors = new ArrayList<>();

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
        if (appointmentDto.getRecurrencePattern() != null) {
            validateRecurrencePattern(appointmentDto, errors);
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
    private void validateRecurrencePattern(CreateAppointmentDto dto, List<String> errors) {
        switch (dto.getRecurrencePattern()) {
            case Daily:
                validateDailyRecurrence(dto, errors);
                break;
            case Weekly:
                validateWeeklyRecurrence(dto, errors);
                break;
            case Only:
                validateSingleRecurrence(dto, errors);
                break;
            default:
                errors.add("Invalid recurrence pattern");
        }
    }

    private void validateDailyRecurrence(CreateAppointmentDto dto, List<String> errors) {
        // Validate recurrence end date is required
        if (dto.getRecurrenceEndDate() == null) {
            errors.add("Recurrence end date is required for daily pattern");
            return;
        }

        // Validate recurrence end date is not before start date
        if (dto.getDate() != null && dto.getRecurrenceEndDate().isBefore(dto.getDate())) {
            errors.add("Recurrence end date must be after or equal to start date");
        }

        // Validate maximum recurrence period (e.g., 1 year)
        if (dto.getDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(dto.getDate(), dto.getRecurrenceEndDate());
            if (daysBetween > 365) { // You can adjust this limit as needed
                errors.add("Daily recurrence period cannot exceed 1 year");
            }
        }
    }

    private void validateWeeklyRecurrence(CreateAppointmentDto dto, List<String> errors) {
        // Validate recurrence end date is required
        if (dto.getRecurrenceEndDate() == null) {
            errors.add("Recurrence end date is required for weekly pattern");
            return;
        }

        // Validate recurrence end date is not before start date
        if (dto.getDate() != null && dto.getRecurrenceEndDate().isBefore(dto.getDate())) {
            errors.add("Recurrence end date must be after or equal to start date");
        }

        // Validate weekly days are specified
        if (dto.getWeeklyDay() == null || dto.getWeeklyDay().isEmpty()) {
            errors.add("At least one day of week must be selected for weekly pattern");
        } else {
            // Validate weekly days contain valid values
            for (DayOfWeek day : dto.getWeeklyDay()) {
                if (!EnumSet.allOf(DayOfWeek.class).contains(day)) {
                    errors.add("Invalid day of week specified: " + day);
                }
            }

            // Remove duplicates and validate
            if (dto.getWeeklyDay().size() != new HashSet<>(dto.getWeeklyDay()).size()) {
                errors.add("Duplicate days of week are not allowed");
            }
        }

        // Validate maximum recurrence period (e.g., 1 year)
        if (dto.getDate() != null) {
            long weeksBetween = ChronoUnit.WEEKS.between(dto.getDate(), dto.getRecurrenceEndDate());
            if (weeksBetween > 52) { // You can adjust this limit as needed
                errors.add("Weekly recurrence period cannot exceed 1 year");
            }
        }
    }

    private void validateSingleRecurrence(CreateAppointmentDto dto, List<String> errors) {
        // For ONLY pattern, recurrence end date should not be set
        if (dto.getRecurrenceEndDate() != null) {
            errors.add("Recurrence end date should not be set for single appointments");
        }

        // Weekly days should not be set
        if (dto.getWeeklyDay() != null && !dto.getWeeklyDay().isEmpty()) {
            errors.add("Weekly days should not be set for single appointments");
        }
    }

    private void validateRoomAvailability(CreateAppointmentDto dto) {
        if (dto.getRoomId() == null) {
            return; // Skip validation if no room is selected
        }

        LocalDate startDate = dto.getDate();
        LocalDate endDate = dto.getRecurrencePattern() != RecurrencePattern.Only
                ? dto.getRecurrenceEndDate()
                : dto.getDate();

        List<Appointment> existingAppointments;

        switch (dto.getRecurrencePattern()) {
            case Daily:
                // Check each day between start and end date
                existingAppointments = appointmentRepository
                        .findOverlappingAppointments(
                                dto.getRoomId(),
                                startDate,
                                endDate,
                                dto.getStartTime(),
                                dto.getEndTime()
                        );
                break;

            case Weekly:
                // Check only on specified days of week between start and end date
                existingAppointments = appointmentRepository
                        .findOverlappingAppointmentsForWeeklyPattern(
                                dto.getRoomId(),
                                startDate,
                                endDate,
                                dto.getStartTime(),
                                dto.getEndTime(),
                                dto.getWeeklyDay()
                        );
                break;

            case Only:
            default:
                // Check only on the specified date
                existingAppointments = appointmentRepository
                        .findOverlappingAppointments(
                                dto.getRoomId(),
                                startDate,
                                startDate,
                                dto.getStartTime(),
                                dto.getEndTime()
                        );
        }

        if (!existingAppointments.isEmpty()) {
            throw new ConflictException("Room is already booked for the specified time period");
        }
    }
}