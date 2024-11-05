package com.example.vida.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserDto {
    private String username;
    private String email;
    private Long departmentId;
    private Integer status;
    private LocalDate dob;
    private String phoneNumber;
    private String gender;
    private String employeeId;
    private String cardId;
}