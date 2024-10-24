package com.example.vida.repository;

import com.example.vida.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
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
    );    @Query("SELECT a FROM Appointment a " +
            "WHERE a.room.id = :roomId " +
            "AND a.date BETWEEN :startDate AND :endDate " +
            "AND ((a.startTime <= :endTime AND a.endTime >= :startTime))")
    List<Appointment> findOverlappingAppointments(
            @Param("roomId") Integer roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.room.id = :roomId " +
            "AND a.date BETWEEN :startDate AND :endDate " +
            "AND FUNCTION('DAYOFWEEK', a.date) IN :weeklyDays " +
            "AND ((a.startTime <= :endTime AND a.endTime >= :startTime))")
    List<Appointment> findOverlappingAppointmentsForWeeklyPattern(
            @Param("roomId") int roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("weeklyDays") List<String> weeklyDays
    );
}
