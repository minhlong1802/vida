package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateAppointmentDto;
import com.example.vida.entity.Appointment;
import com.example.vida.entity.Room;
import com.example.vida.entity.User;
import com.example.vida.enums.RecurrencePattern;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.repository.AppointmentRepository;
import com.example.vida.repository.RoomRepository;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.AppointmentService;
import com.example.vida.utils.DateUtils;
import com.example.vida.utils.UserContext;
import jakarta.persistence.criteria.*;
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
import java.time.format.DateTimeFormatter;
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
    private static final Set<String> VALID_WEEKLY_DAYS = Set.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
    );
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

    private static final String TIME_PATTERN = "HH:mm";
    private static final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern(TIME_PATTERN);

    public Map<String, Object> searchAppointmentByTitle(String searchText, Integer roomId, int page, int size, Integer userId){
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
                        predicates.add(criteriaBuilder.equal(root.get("room").get("id"), roomId));
                    }
                    if (userId != null ) {
                        // Join with the users relationship
                        Join<Appointment, User> userJoin = root.join("users");
                        // Add condition where user ID is in the provided list
                        predicates.add(userJoin.get("id").in(userId));
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
                    List<DayOfWeek> weeklyDays = convertToDayOfWeek(createAppointmentDto.getWeeklyDay());
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

    public Map<String, String> validateAppointmentData(CreateAppointmentDto appointmentDto) {
        // 1. Time validations
        LocalDate currentDate = LocalDate.now();
        Map<String, String> errors = new HashMap<>();
        LocalDate date = null;
        LocalTime startTime = null;
        LocalTime endTime = null;

        // Validate date
        if (!dateUtils.isValidDate(appointmentDto.getDate())) {
            errors.put("date", "Date is invalid format");
        } else {
            try {
                date = LocalDate.parse(appointmentDto.getDate(), formatterDate);

                // Thêm validation cho date nếu cần
                if (date.isBefore(LocalDate.now())) {
                    errors.put("date", "Date cannot be in the past");
                }
            } catch (Exception e) {
                errors.put("date", "Date is invalid format");
            }
        }

        // Validate startTime
        if (!isValidTime(appointmentDto.getStartTime())) {
            errors.put("startTime", "Start time is invalid format");
        } else {
            try {
                startTime = LocalTime.parse(appointmentDto.getStartTime(), formatterTime);
            } catch (Exception e) {
                errors.put("startTime", "Start time is invalid format");
            }
        }

        // Validate endTime
        if (!isValidTime(appointmentDto.getEndTime())) {
            errors.put("endTime", "End time is invalid format");
        } else {
            try {
                endTime = LocalTime.parse(appointmentDto.getEndTime(), formatterTime);
            } catch (Exception e) {
                errors.put("endTime", "End time is invalid format");
            }
        }

        // Check if appointment date is in the past
        if (date.isBefore(currentDate)) {
            errors.put("date","Appointment date cannot be in the past");
        }
        if (RecurrencePattern.Weekly.equals(appointmentDto.getRecurrencePattern())) {
            List<String> weeklyDays = appointmentDto.getWeeklyDay();
            if (weeklyDays == null || weeklyDays.isEmpty()) {
                errors.put("weeklyDay", "Weekly days must be specified for weekly recurring appointments");
            } else {
                // Kiểm tra format và giá trị hợp lệ của các ngày
                List<String> invalidDays = weeklyDays.stream()
                        .filter(day -> !isValidWeekDay(day))
                        .collect(Collectors.toList());

                if (!invalidDays.isEmpty()) {
                    errors.put("weeklyDay",
                            "Invalid week day value(s): " + String.join(", ", invalidDays) +
                                    ". Valid values are: " + String.join(", ", VALID_WEEKLY_DAYS));
                }
            }
        }
        // Validate recurrence end date is not before start date
        if (appointmentDto.getRecurrenceEndDate().isBefore(date)) {
            errors.put("recurrenceEndDate","Recurrence end date must be after or equal to start date");
        }
        // Check if end time is before start time
        if (endTime.isBefore(startTime)) {
            errors.put("endTime","End time must be after start time");
        }

        // Check recurrence end date if present
        if (appointmentDto.getRecurrencePattern() != RecurrencePattern.Only) {
            if (appointmentDto.getRecurrenceEndDate().isBefore(date)) {
                errors.put("recurrenceEndDate","Recurrence end date must be after appointment date");
            }
        }
        // 2. Conflict validations
        List<Appointment> existingAppointments = appointmentRepository
                .findConflictingAppointments(
                        appointmentDto.getRoomId(),
                        date,
                        startTime,
                        endTime
                );


        // Check for time overlaps with existing appointments
        boolean hasConflict = existingAppointments.stream()
                .anyMatch(existing -> isTimeOverlap(
                        startTime,
                        endTime,
                        existing.getStartTime(),
                        existing.getEndTime()
                ));

        if (hasConflict) {
            errors.put("date","Conflict with existing appointment");
        }
        return errors;
    }
    private boolean isValidWeekDay(String day) {
        if (day == null) return false;
        return VALID_WEEKLY_DAYS.contains(day.toUpperCase());
    }
    private List<DayOfWeek> convertToDayOfWeek(List<String> weeklyDays) {
        List<DayOfWeek> convertedDays = new ArrayList<>();

        for (String day : weeklyDays) {
            try {
                convertedDays.add(DayOfWeek.valueOf(day.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Must be day of week");
            }
        }

        return convertedDays;
    }
    public static boolean isValidTime(String time) {
        try {
            // Check if time string is null or empty
            if (time == null || time.trim().isEmpty()) {
                return false;
            }

            // Define regex pattern for time format HH:mm
            // HH: 00-23
            // mm: 00-59
            String timePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";

            // First check if the format matches our pattern
            if (!time.matches(timePattern)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private boolean isTimeOverlap(
            LocalTime start1, LocalTime end1,
            LocalTime start2, LocalTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }
}