package com.example.vida.repository;

import com.example.vida.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer>, JpaSpecificationExecutor<Appointment> {
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.room.id = :roomId " +
            "AND a.date = :date " +
            "AND ((a.startTime <= :endTime AND a.endTime >= :startTime) " +
            "OR (a.startTime >= :startTime AND a.startTime < :endTime))")
    List<Appointment> findConflictingAppointments(
            @Param("roomId") Integer roomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
    void deleteByIdAndDateGreaterThanEqual(Integer id, LocalDate fromDate);

}
