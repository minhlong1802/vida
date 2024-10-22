package com.example.vida.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private Integer departmentId;
    private Integer status;
    private LocalDate dob;
    private String phoneNumber;
    private String gender;
    private String employeeId;
    private String cardId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String creatorName;
    private String updatorName;
}