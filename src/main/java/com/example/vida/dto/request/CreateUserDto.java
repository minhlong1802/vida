package com.example.vida.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserDto implements Serializable {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;



    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;


    private Integer departmentId;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Pattern(regexp = "0\\d{9}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull(message = "Gender is required")
    private String gender;


    private String employeeId;


    @JsonAlias("card_id")
    private String cardId;

    private Integer status;




//    public enum Gender {
//        MALE("Male"),
//        FEMALE("Female"),
//        OTHER("Other");
//
//        private final String value;
//
//        Gender(String value) {
//            this.value = value;
//        }
//
//    }
}