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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public Map<String, Object> searchAppointmentByTitle(String searchText, Integer roomId, int page, int size, List<Integer> userIds){
        try {
            if (page > 0) {
                page = page - 1;
            }
            Pageable pageable = PageRequest.of(page, size);
            Specification<Appointment> specification = new Specification<Appointment>() {
                @Override
                public Predicate toPredicate(Root<Appointment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.like(root.get("title"), "%" + searchText + "%"));
                    if (roomId != null) {
                        predicates.add(criteriaBuilder.equal(root.get("roomId"), roomId));
                    }
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };

            Page<Appointment> pageAppointment = appointmentRepository.findAll(specification, pageable);

            Map<String, Object> mapDepartment = new HashMap<>();
            mapDepartment.put("listAppointment", pageAppointment.getContent());
            mapDepartment.put("pageSize", pageAppointment.getSize());
            mapDepartment.put("pageNo", pageAppointment.getNumber()+1);
            mapDepartment.put("totalPage", pageAppointment.getTotalPages());
            return mapDepartment;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Appointment createAppointment(CreateAppointmentDto createAppointmentDto) {
        validateAppointmentData(createAppointmentDto);

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
                    List<String> errors = new ArrayList<>();
                    List<DayOfWeek> weeklyDays = convertToDayOfWeek(createAppointmentDto.getWeeklyDay(), errors);
                    if (!errors.isEmpty()) {
                        throw new IllegalArgumentException(String.join(", ", errors));
                    }
                    appointments.addAll(createWeeklyAppointments(baseAppointment, weeklyDays));
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
        // Check if appointment date is in the past
        if (appointmentDto.getDate().isBefore(currentDate)) {
            throw new IllegalArgumentException("Appointment date cannot be in the past");
        }
        if (appointmentDto.getRecurrenceEndDate() == null) {
            throw new IllegalArgumentException("Recurrence end date is required");

        }

        // Validate recurrence end date is not before start date
        if (appointmentDto.getRecurrenceEndDate().isBefore(appointmentDto.getDate())) {
            throw new IllegalArgumentException("Recurrence end date must be after or equal to start date");
        }
        // Check if end time is before start time
        if (appointmentDto.getEndTime().isBefore(appointmentDto.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Check recurrence end date if present
        if (appointmentDto.getRecurrencePattern() != RecurrencePattern.Only) {
            if (appointmentDto.getRecurrenceEndDate().isBefore(appointmentDto.getDate())) {
                throw new IllegalArgumentException("Recurrence end date must be after appointment date");
            }
        }
        // 2. Conflict validations
        List<Appointment> existingAppointments = appointmentRepository
                .findConflictingAppointments(
                        appointmentDto.getRoomId(),
                        appointmentDto.getDate(),
                        appointmentDto.getStartTime(),
                        appointmentDto.getEndTime()
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
    private List<DayOfWeek> convertToDayOfWeek(List<String> weeklyDays, List<String> errors) {
        List<DayOfWeek> convertedDays = new ArrayList<>();

        for (String day : weeklyDays) {
            try {
                convertedDays.add(DayOfWeek.valueOf(day.toUpperCase()));
            } catch (IllegalArgumentException e) {
                errors.add("weeklyDay must be day of week");
            }
        }

        return convertedDays;
    }
    private boolean isTimeOverlap(
            LocalTime start1, LocalTime end1,
            LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}