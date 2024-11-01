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
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 20)
    private String password;

    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(name = "department_id")
    private Integer departmentId;

    @ManyToOne
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;


    @Column(nullable = false)
    private Integer status;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private String gender;

    @Column(name = "employee_id", length = 10)
    private String employeeId;

    @Column(name = "card_id", length = 10)
    private String cardId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "creator_name", length = 20)
    private String creatorName;

    @Column(name = "updator_id")
    private Integer updatorId;

    @Column(name = "updator_name", length = 20)
    private String updatorName;

//    @Getter
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