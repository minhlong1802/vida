package com.example.vida.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoomDto {
    @NotBlank(message = "Dữ liệu gửi lên không đúng định dạng")
    @NotBlank(message = "Room name is required")
    private String roomName;

    @NotNull(message = "Dữ liệu gửi lên không đúng định dạng")
    @Min(value = 1, message = "Capacity must be greater than 0")
    @Max(value = 100)
    private Integer capacity;

    @NotBlank(message = "Location is required")
    private String location;
}
