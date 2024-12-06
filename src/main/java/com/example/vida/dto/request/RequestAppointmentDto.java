package com.example.vida.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestAppointmentDto {
    @NotBlank(message = "Title is required")
    @NotNull(message = "Title is required")
    @Size(max = 50, message = "Title must not exceed 50 characters")
    private String title;

    private String seriesId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestAppointmentDto that = (RequestAppointmentDto) o;
        return Objects.equals(title, that.title) && Objects.equals(seriesId, that.seriesId) && Objects.equals(roomId, that.roomId) && Objects.equals(date, that.date) && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(contentBrief, that.contentBrief) && Objects.equals(recurrencePattern, that.recurrencePattern) && Objects.equals(recurrenceEndDate, that.recurrenceEndDate) && Objects.equals(userIds, that.userIds) && Objects.equals(weeklyDays, that.weeklyDays) && Objects.equals(updaterSelection, that.updaterSelection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, seriesId, roomId, date, startTime, endTime, contentBrief, recurrencePattern, recurrenceEndDate, userIds, weeklyDays, updaterSelection);
    }
}
