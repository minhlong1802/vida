package com.example.vida.dto.request;

import com.example.vida.enums.RecurrencePattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentDto {
    @NotBlank(message = "Title is required")
    @NotNull(message = "Title is required")
    @Size(max = 50, message = "Title must not exceed 50 characters")
    private String title;

    @NotNull(message = "Room ID is required")
    private Integer roomId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private String contentBrief;

    @NotNull(message = "Recurrence pattern is required and enum 'daily, only, weekly')")
    private RecurrencePattern recurrencePattern = RecurrencePattern.Only;

    private LocalDate recurrenceEndDate;

    private Set<Integer> userIds = new HashSet<>();

    private List<DayOfWeek> weeklyDay;
}
