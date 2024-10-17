package com.example.vida.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String password;
    private String email;
    private int departmentId;
    private int status;
    private LocalDate dob;
    private String phoneNumber;
    private String cardId;
    private LocalDateTime createdAt;
    private int creatorId;
    private String creatorName;
    private LocalDateTime updatedAt;
    private int updatorId;
    private String updatorName;
}
