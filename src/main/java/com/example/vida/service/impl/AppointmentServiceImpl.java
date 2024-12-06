package com.example.vida.service.impl;

import com.example.vida.dto.request.DeleteRequest;
import com.example.vida.dto.request.RequestAppointmentDto;
import com.example.vida.dto.response.UnavailableTimeSlotDTO;
import com.example.vida.entity.Appointment;
import com.example.vida.entity.Room;
import com.example.vida.entity.User;
import com.example.vida.enums.RecurrencePattern;
import com.example.vida.exception.*;
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
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.vida.enums.RecurrencePattern.DAILY;
import static com.example.vida.enums.RecurrencePattern.WEEKLY;

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

    public Map<String, Object> searchAppointmentByTitle(String searchText, Integer roomId, int pageNo, int pageSize, Integer userId){
        try {
            if (pageNo > 0) {
                pageNo = pageNo - 1;
            }
            Pageable pageable = PageRequest.of(pageNo, pageSize);
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

    public Appointment createAppointment(RequestAppointmentDto requestAppointmentDto) {
        validateAppointmentData(requestAppointmentDto);

        List<Appointment> appointments = new ArrayList<>();

        // Create base appointment
        Appointment baseAppointment = createBaseAppointment(requestAppointmentDto);

        // Handle recurring appointments
        if (baseAppointment.getRecurrencePattern() != null) {
            switch (baseAppointment.getRecurrencePattern()) {
                case DAILY:
                    appointments.addAll(createDailyAppointments(baseAppointment));
                    break;
                case WEEKLY:
                    List<DayOfWeek> weeklyDays = convertToDayOfWeek(requestAppointmentDto.getWeeklyDays());
                    appointments.addAll(createWeeklyAppointments(baseAppointment, weeklyDays));
                    break;
                case ONLY:
                    baseAppointment.setSeriesId(null);
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

    private Appointment createBaseAppointment(RequestAppointmentDto requestAppointmentDto) {
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(requestAppointmentDto, appointment);
        // Set room
        if (requestAppointmentDto.getRoomId() != null) {
            Room room = roomRepository.findById(requestAppointmentDto.getRoomId())
                    .orElseThrow(() -> new RoomNotFoundException("Room not found"));
            appointment.setRoom(room);
        }
        appointment.setDate(LocalDate.parse(requestAppointmentDto.getDate()));
        appointment.setStartTime(LocalTime.parse(requestAppointmentDto.getStartTime()));
        appointment.setEndTime(LocalTime.parse(requestAppointmentDto.getEndTime()));
        appointment.setRecurrenceEndDate(LocalDate.parse(requestAppointmentDto.getRecurrenceEndDate()));
        appointment.setRecurrencePattern(RecurrencePattern.valueOf(requestAppointmentDto.getRecurrencePattern().toUpperCase()));
        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        appointment.setCreatedAt(now);
        appointment.setUpdatedAt(now);
        appointment.setCreatorId(UserContext.getUser().getUserId());
        appointment.setCreatorName(UserContext.getUser().getUsername());
        appointment.setUpdatorId(UserContext.getUser().getUserId());
        appointment.setUpdatorName(UserContext.getUser().getUsername());
        appointment.setSeriesId(String.valueOf(UUID.randomUUID()));

        User currentUser = userRepository.findById(UserContext.getUser().getUserId())
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        // Initialize users set and add creator
        Set<User> users = new HashSet<>();
        users.add(currentUser);
        // Set users
        if (requestAppointmentDto.getUserIds() != null && !requestAppointmentDto.getUserIds().isEmpty()) {
            Set<User> additionalUsers = requestAppointmentDto.getUserIds().stream()
                    .filter(userId -> !userId.equals(currentUser.getId())) // Skip if creator is already in the list
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)))
                    .collect(Collectors.toSet());
            users.addAll(additionalUsers);
            appointment.setUsers(users);
        }

        return appointment;
    }

    private List<Appointment> createDailyAppointments(Appointment baseAppointment) {
        List<Appointment> appointments = new ArrayList<>();
        LocalDate startDate = baseAppointment.getDate();
        LocalDate endDate = baseAppointment.getRecurrenceEndDate();

        // Validate all dates first
        Map<String, String> errors = new HashMap<>();
        Map<LocalDate, String> conflicts = new HashMap<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            checkForConflicts(
                    baseAppointment.getRoom().getId(),
                    currentDate,
                    baseAppointment.getStartTime(),
                    baseAppointment.getEndTime(),
                    baseAppointment.getSeriesId(),
                    errors
            );

            if (errors.containsKey("date")) {
                conflicts.put(currentDate, errors.get("date"));
                errors.clear();
            }
            currentDate = currentDate.plusDays(1);
        }

        if (!conflicts.isEmpty()) {
            throw new ConflictException(conflicts);
        }

        // Create appointments if no conflicts
        while (!startDate.isAfter(endDate)) {
            Appointment newAppointment = cloneAppointment(baseAppointment);
            newAppointment.setDate(startDate);
            newAppointment.setRecurrencePattern(DAILY);
            appointments.add(newAppointment);
            startDate = startDate.plusDays(1);
        }

        return appointments;
    }

    private List<Appointment> createWeeklyAppointments(Appointment baseAppointment, List<DayOfWeek> weeklyDays) {
        List<Appointment> appointments = new ArrayList<>();
        LocalDate startDate = baseAppointment.getDate();
        LocalDate endDate = baseAppointment.getRecurrenceEndDate();

        // Validate all dates first
        Map<String, String> errors = new HashMap<>();
        Map<LocalDate, String> conflicts = new HashMap<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (weeklyDays.contains(currentDate.getDayOfWeek())) {
                checkForConflicts(
                        baseAppointment.getRoom().getId(),
                        currentDate,
                        baseAppointment.getStartTime(),
                        baseAppointment.getEndTime(),
                        baseAppointment.getSeriesId(),
                        errors
                );

                if (errors.containsKey("date")) {
                    conflicts.put(currentDate, errors.get("date"));
                    errors.clear();
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        if (!conflicts.isEmpty()) {
            throw new ConflictException(conflicts);
        }

        // Create appointments if no conflicts
        while (!startDate.isAfter(endDate)) {
            if (weeklyDays.contains(startDate.getDayOfWeek())) {
                Appointment newAppointment = cloneAppointment(baseAppointment);
                newAppointment.setDate(startDate);
                newAppointment.setRecurrencePattern(WEEKLY);
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

    public Map<String, String> validateAppointmentData(RequestAppointmentDto appointmentDto) {
        Map<String, String> errors = new HashMap<>();

        // Validate recurrence pattern first since it affects other validations
        if (!isValidRecurrencePattern(appointmentDto.getRecurrencePattern())) {
            errors.put("recurrencePattern", "Invalid recurrence pattern. Valid values are: " +
                    Arrays.stream(RecurrencePattern.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
            return errors;
        }
        // Convert string to enum once it's validated
        RecurrencePattern recurrencePattern = RecurrencePattern.valueOf(appointmentDto.getRecurrencePattern().toUpperCase());
        LocalDate currentDate = LocalDate.now();
        LocalDate date = parseAndValidateDate(appointmentDto.getDate(), "date", currentDate, errors);
        LocalDate recurrenceEndDate = null;

        // Only validate recurrenceEndDate if pattern is not ONLY
        if (!RecurrencePattern.ONLY.equals(recurrencePattern)) {
            recurrenceEndDate = parseAndValidateDate(appointmentDto.getRecurrenceEndDate(), "recurrenceEndDate", currentDate, errors);

            // Additional validation for recurrenceEndDate if both dates are valid
            if (date != null && recurrenceEndDate != null && recurrenceEndDate.isBefore(date)) {
                errors.put("recurrenceEndDate", "Recurrence end date must be after or equal to start date");
            }
        }
        LocalTime startTime = parseAndValidateTime(appointmentDto.getStartTime(), "startTime", errors);
        LocalTime endTime = parseAndValidateTime(appointmentDto.getEndTime(), "endTime", errors);

        // If basic parsing failed, return early
        if (!errors.isEmpty()) {
            return errors;
        }

        // Validate appointment timing logic
        validateAppointmentTiming(date, startTime, endTime, errors);

        // Validate recurrence
        validateRecurrence(appointmentDto, date,recurrenceEndDate,recurrencePattern,errors);

        // Check for conflicts only if no other errors
//        if (errors.isEmpty()) {
//            checkForConflicts(appointmentDto.getRoomId(), date, startTime, endTime, errors);
//        }
        if (appointmentDto.getUpdaterSelection() != null) {
            if (!appointmentDto.getUpdaterSelection().equals(1) && !appointmentDto.getUpdaterSelection().equals(2)) {
                errors.put("updaterSelection", "Invalid selection");
            }
        }

        return errors;
    }
    private boolean isValidRecurrencePattern(String pattern) {
        if (pattern == null) return false;
        try {
            RecurrencePattern.valueOf(pattern.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    private LocalDate parseAndValidateDate(String dateStr, String fieldName, LocalDate currentDate, Map<String, String> errors) {
        if (dateStr == null) {
            errors.put(fieldName, fieldName + " is required");
            return null;
        }

        if (!DateUtils.isValidDate(dateStr)) {
            errors.put(fieldName, fieldName + " is invalid format");
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, formatterDate);
            if (date.isBefore(currentDate)) {
                errors.put(fieldName, fieldName + " cannot be in the past");
            }
            return date;
        } catch (DateTimeParseException e) {
            errors.put(fieldName, fieldName + " is invalid format");
            return null;
        }
    }

    private LocalTime parseAndValidateTime(String timeStr, String fieldName, Map<String, String> errors) {
        if (!isValidTime(timeStr)) {
            errors.put(fieldName, fieldName + " is invalid format");
            return null;
        }

        try {
            return LocalTime.parse(timeStr, formatterTime);
        } catch (DateTimeParseException e) {
            errors.put(fieldName, fieldName + " is invalid format");
            return null;
        }
    }

    private void validateAppointmentTiming(LocalDate date, LocalTime startTime, LocalTime endTime,
                                           Map<String, String> errors) {
        if (date != null && startTime != null && endTime != null) {
            if (endTime.isBefore(startTime)) {
                errors.put("endTime", "End time must be after start time");
            }
        }
    }

    private void validateRecurrence(RequestAppointmentDto appointmentDto, LocalDate date, LocalDate recurrenceEndDate, RecurrencePattern recurrencePattern,
                                    Map<String, String> errors) {
        if (WEEKLY.equals(recurrencePattern)) {
            validateWeeklyRecurrence(appointmentDto, errors);
        }

        if (!RecurrencePattern.ONLY.equals(recurrencePattern)) {
            if (recurrenceEndDate != null && recurrenceEndDate.isBefore(date)) {
                errors.put("recurrenceEndDate", "Recurrence end date must be after or equal to start date");
            }
        }
    }

    private void validateWeeklyRecurrence(RequestAppointmentDto appointmentDto, Map<String, String> errors) {
        List<String> weeklyDays = appointmentDto.getWeeklyDays();
        if (weeklyDays == null || weeklyDays.isEmpty()) {
            errors.put("weeklyDays", "Weekly days must be specified for weekly recurring appointments");
            return;
        }

        List<String> invalidDays = weeklyDays.stream()
                .filter(day -> !isValidWeekDay(day))
                .toList();

        if (!invalidDays.isEmpty()) {
            errors.put("weeklyDay",
                    String.format("Invalid week day value(s): %s. Valid values are: %s",
                            String.join(", ", invalidDays),
                            String.join(", ", VALID_WEEKLY_DAYS)));
        }
    }

    private void checkForConflicts(int roomId, LocalDate date,
                                   LocalTime startTime, LocalTime endTime,
                                   String currentSeriesId,
                                   Map<String, String> errors) {
        // Find conflicting appointments
        List<Appointment> existingAppointments = appointmentRepository
                .findConflictingAppointments(roomId, date, startTime, endTime);

        // Filter out conflicts
        List<Appointment> actualConflicts = existingAppointments.stream()
                .filter(existing -> {
                    // Ignore appointments in the same series
                    if (currentSeriesId != null &&
                            existing.getSeriesId() != null &&
                            currentSeriesId.equals(existing.getSeriesId())) {
                        return false;
                    }

                    // Check for actual time overlap
                    return isTimeOverlap(startTime, endTime,
                            existing.getStartTime(), existing.getEndTime());
                })
                .toList();

        // If there are actual conflicts, add error
        if (!actualConflicts.isEmpty()) {
            errors.put("date", "Conflict with existing appointment");
        }
    }
    private void checkForUpdateConflicts(Integer appointmentId, int roomId, LocalDate date,
                                         LocalTime startTime, LocalTime endTime,
                                         Map<String, String> errors) {
        // Get the appointment being updated to identify its series
        Appointment currentAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + appointmentId));


        // 2. Check conflicts with other appointments (not in the series)
        List<Appointment> otherAppointments = appointmentRepository
                .findConflictingAppointmentsExcludingSeries(
                        roomId,
                        date,
                        startTime,
                        endTime,
                        currentAppointment.getRoom(),
                        currentAppointment.getStartTime(),
                        currentAppointment.getEndTime()
                );

        if (!otherAppointments.isEmpty()) {
            errors.put("date", "Conflict with existing appointment in the room");
        }
    }
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return !end1.isBefore(start2) && !end2.isBefore(start1);
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
    public Appointment updateAppointment(Integer id, RequestAppointmentDto requestAppointmentDto) {
        log.debug("Starting appointment update process for id: {}", id);
        Map<String, String> errors = validateAppointmentData(requestAppointmentDto);
        // Check for conflicts
        checkForUpdateConflicts(
                id,
                requestAppointmentDto.getRoomId(),
                LocalDate.parse(requestAppointmentDto.getDate()),
                LocalTime.parse(requestAppointmentDto.getStartTime()),
                LocalTime.parse(requestAppointmentDto.getEndTime()),
                errors
        );

        // First find the existing appointment
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
        try {
            // Get current user first to fail fast if user not found
            User currentUser = userRepository.findById(UserContext.getUser().getUserId())
                    .orElseThrow(() -> new UserNotFoundException("Current user not found"));

            if (requestAppointmentDto.getUpdaterSelection() == 1) {
                return updateSingleAppointment(existingAppointment, requestAppointmentDto, currentUser);
            } else if (requestAppointmentDto.getUpdaterSelection() == 2) {
                return updateRecurringAppointments(existingAppointment, requestAppointmentDto, currentUser);
            } else {
                throw new AppointmentValidationException("Invalid updater selection value");
            }

        } catch (AppointmentNotFoundException | AppointmentValidationException | UserNotFoundException | RoomNotFoundException | ConflictException e) {
            // Log and re-throw these specific exceptions without wrapping
            log.warn("Validation or not found error: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            // Log unexpected errors
            log.error("Unexpected error updating appointment: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating appointment", e);
        }
    }

    private Appointment updateSingleAppointment(Appointment appointment, RequestAppointmentDto requestAppointmentDto, User currentUser) {
        try {
            List<Appointment> appointmentList=appointmentRepository.findConflictingAppointments(requestAppointmentDto.getRoomId(), LocalDate.parse(requestAppointmentDto.getDate()),LocalTime.parse(requestAppointmentDto.getStartTime()),LocalTime.parse(requestAppointmentDto.getEndTime()));
            List<Appointment> actualConflicts = appointmentList.stream()
                    .filter(existing -> {
                        // Check for actual time overlap
                        return isTimeOverlap(LocalTime.parse(requestAppointmentDto.getStartTime()), LocalTime.parse(requestAppointmentDto.getEndTime()),
                                existing.getStartTime(), existing.getEndTime());
                    })
                    .toList();

            // If there are actual conflicts, add error
            if (actualConflicts.size()>1) {
                throw new AppointmentValidationException("Conflict with existing appointment");
            }
            // Copy properties from DTO to entity
            BeanUtils.copyProperties(requestAppointmentDto, appointment);

            // Update room if provided
            if (requestAppointmentDto.getRoomId() != null) {
                Room room = roomRepository.findById(requestAppointmentDto.getRoomId())
                        .orElseThrow(() -> new RoomNotFoundException("Room not found"));
                appointment.setRoom(room);
            }

            // Set date and time fields
            appointment.setDate(LocalDate.parse(requestAppointmentDto.getDate()));
            appointment.setStartTime(LocalTime.parse(requestAppointmentDto.getStartTime()));
            appointment.setEndTime(LocalTime.parse(requestAppointmentDto.getEndTime()));
            appointment.setRecurrenceEndDate(LocalDate.parse(requestAppointmentDto.getRecurrenceEndDate()));

            // Update audit fields
            updateAuditFields(appointment);

            // Update users
            updateAppointmentUsers(appointment, requestAppointmentDto, currentUser);

            return appointmentRepository.save(appointment);

        } catch (RoomNotFoundException e){
            throw new RoomNotFoundException("Room with id"+requestAppointmentDto.getRoomId()+" not found");
        }catch (Exception e) {
            log.error("Error updating single appointment: {}", e.getMessage(), e);
            if (e instanceof AppointmentValidationException || e instanceof UserNotFoundException) {
                throw e;
            }
            throw new AppointmentValidationException("Error updating appointment: " + e.getMessage());
        }
    }

    private Appointment updateRecurringAppointments(Appointment existingAppointment, RequestAppointmentDto requestAppointmentDto, User currentUser) {
        try {
            deleteRecurringAppointmentsFromDate(existingAppointment.getId());
            // Create new base appointment
            Appointment baseAppointment = createBaseAppointment(requestAppointmentDto);

            // Create recurring appointments based on recurrence type
            List<Appointment> newAppointments = new ArrayList<>();
            switch (baseAppointment.getRecurrencePattern()) {
                case DAILY:
                    newAppointments = createDailyAppointments(baseAppointment);
                    break;
                case WEEKLY:
                    List<DayOfWeek> weeklyDays = convertToDayOfWeek(requestAppointmentDto.getWeeklyDays());
                    newAppointments = createWeeklyAppointments(baseAppointment,weeklyDays);
                    break;
                default:
                    throw new AppointmentValidationException("Unsupported recurrence type");
            }

            // Save all new appointments
            for (Appointment appointment : newAppointments) {
                updateAuditFields(appointment);
                updateAppointmentUsers(appointment, requestAppointmentDto, currentUser);
                appointmentRepository.save(appointment);
            }

            return newAppointments.isEmpty() ? null : newAppointments.get(0);

        } catch (ConflictException e){
            log.warn("Conflict error: {}", e.getMessage());
            throw e;
        }catch (Exception e) {
            log.error("Error updating recurring appointments: {}", e.getMessage(), e);
            if (e instanceof AppointmentValidationException || e instanceof UserNotFoundException) {
                throw e;
            }
            throw new AppointmentValidationException("Error updating recurring appointments: " + e.getMessage());
        }
    }

    private void updateAuditFields(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();
        appointment.setUpdatedAt(now);
        appointment.setUpdatorId(UserContext.getUser().getUserId());
        appointment.setUpdatorName(UserContext.getUser().getUsername());
    }

    private void updateAppointmentUsers(Appointment appointment, RequestAppointmentDto requestAppointmentDto, User currentUser) {
        Set<User> users = new HashSet<>();
        users.add(currentUser);

        if (requestAppointmentDto.getUserIds() != null && !requestAppointmentDto.getUserIds().isEmpty()) {
            Set<User> additionalUsers = requestAppointmentDto.getUserIds().stream()
                    .filter(userId -> !userId.equals(currentUser.getId()))
                    .map(userId -> userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)))
                    .collect(Collectors.toSet());
            users.addAll(additionalUsers);
        }

        appointment.setUsers(users);
    }
    public void deleteRecurringAppointmentsFromDate(Integer selectedAppointmentId) {
        // Get the chosen appointment details
        Appointment selectedAppointment = appointmentRepository.findById(selectedAppointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + selectedAppointmentId));

        // Extract pattern information from the selected appointment
        LocalDate selectedDate = selectedAppointment.getDate();
        LocalTime startTime = selectedAppointment.getStartTime();
        LocalTime endTime = selectedAppointment.getEndTime();
        Room room = selectedAppointment.getRoom();

        // Validate selected date is not in the past
        LocalDate now = LocalDate.now();
        if (selectedDate.isBefore(now)) {
            throw new IllegalArgumentException("Cannot delete past appointments. Selected date: " + selectedDate);
        }

        // Delete future appointments with same room and time pattern
        int deletedCount = appointmentRepository.deleteAppointmentsWithSamePatternFromDate(
                room,
                startTime,
                endTime,
                selectedDate
        );

        log.info("Deleted {} future appointments with same pattern starting from date {}. Room: {}, Time: {}-{}",
                deletedCount, selectedDate, room.getId(), startTime, endTime);
    }
    public void deleteAppointments(DeleteRequest request) {
        List<Integer> ids = request.getIds();
        List<Appointment> appointmentsToDelete = appointmentRepository.findAllById(ids);

        List<Integer> existingIds = appointmentsToDelete.stream()
                .map(Appointment::getId)
                .toList();

        List<Integer> notFoundIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new AppointmentNotFoundException("Appointments not found for ids: " + notFoundIds);
        }

        appointmentRepository.deleteAll(appointmentsToDelete);
    }

    public Appointment getAppointmentById(Integer id){
        return appointmentRepository.findById(id).orElseThrow(() -> new AppointmentNotFoundException("Todo with id " + id + " not found"));
    }

    public List<UnavailableTimeSlotDTO> getUnavailableTimeByRoomId(String roomId, String date) {
        try {
            int roomID = Integer.parseInt(roomId);
            if (!roomRepository.existsById(roomID)) {
                throw new RoomNotFoundException("Room with id=" + roomId + " not found!");
            }

            if (!DateUtils.isValidDate(date)) {
                throw new ValidationException("Date is in an invalid format");
            }

            LocalDate inputDate = DateUtils.stringToLocalDate(date, DEFAULT_DATE_FORMAT);
            if (inputDate == null) {
                throw new ValidationException("Date is in an invalid format");
            }

            List<Appointment> appointments = appointmentRepository.findByRoomAndDate(roomID, inputDate);
            List<UnavailableTimeSlotDTO> unavailableTimeSlots = new ArrayList<>();

            for (Appointment appointment : appointments) {
                UnavailableTimeSlotDTO slot = new UnavailableTimeSlotDTO();
                slot.setStartTime(appointment.getStartTime());
                slot.setEndTime(appointment.getEndTime());
                slot.setTitle(appointment.getTitle());
                slot.setUsers(appointment.getUsers());

//                if (!unavailableTimeSlots.isEmpty()) {
//                    UnavailableTimeSlotDTO lastSlot = unavailableTimeSlots.get(unavailableTimeSlots.size() - 1);
//                    if (!lastSlot.getEndTime().isBefore(slot.getStartTime())) {
//                        lastSlot.setEndTime(
//                                lastSlot.getEndTime().isAfter(slot.getEndTime())
//                                        ? lastSlot.getEndTime()
//                                        : slot.getEndTime()
//                        );
//                        continue;
//                    }
//                }
                unavailableTimeSlots.add(slot);
            }

            return unavailableTimeSlots;

        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid room ID format: " + roomId);
        }
    }
}