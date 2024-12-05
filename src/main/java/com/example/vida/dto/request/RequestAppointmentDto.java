package com.example.vida.dto.request;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestAppointmentDto {
    @NotBlank(message = "Title is required")
    @NotNull(message = "Title is required")
    @Size(max = 50, message = "Title must not exceed 50 characters")
    private String title;

    @NotNull(message = "Room ID is required")
    @Positive(message = "Room ID must be greater than 0")
    private Integer roomId;

    @NotNull(message = "Date is required")
    private String date;

    @NotNull(message = "Start time is required")
    private String startTime;

    @NotNull(message = "End time is required")
    private String endTime;

    private String contentBrief;

    @NotNull(message = "Recurrence pattern is required")
    private String recurrencePattern;

    private String recurrenceEndDate;

    private Set<Integer> userIds = new HashSet<>();

    private List<String> weeklyDays;

    private Integer updaterSelection;
}
