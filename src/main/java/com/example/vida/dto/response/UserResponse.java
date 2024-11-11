package com.example.vida.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private Integer id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Department ID is required")
    private Integer departmentId;

    @NotNull(message = "Status is required")
    private Integer status;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Pattern(regexp = "0\\d{9}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    @Pattern(regexp = "(Male|Female|Other)", message = "Invalid gender")
    private String gender;

    private String employeeId;

    private String cardId;

}