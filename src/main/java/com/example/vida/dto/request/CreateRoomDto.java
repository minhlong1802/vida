package com.example.vida.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoomDto {
    @NotBlank(message = "Room name is required")
    private String roomName;

    @Min(value = 1, message = "Capacity must be greater than 0")
    @Max(value = 200)
    @NotNull(message = "Capacity is required")
    private Integer capacity;

    @NotBlank(message = "Location is required")
    private String location;
}
