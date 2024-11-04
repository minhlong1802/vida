package com.example.vida.repository;

import com.example.vida.entity.Appointment;
import com.example.vida.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
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
    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.room = :room " +
            "AND a.startTime = :startTime " +
            "AND a.endTime = :endTime " +
            "AND a.date >= :fromDate")
    int deleteAppointmentsWithSamePatternFromDate(
            @Param("room") Room room,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("fromDate") LocalDate fromDate
    );
    @Query("SELECT a FROM Appointment a WHERE a.room.id = :roomId " +
            "AND a.date = :date " +
            "AND ((a.room != :seriesRoom) " +
            "OR (a.startTime != :seriesStartTime) " +
            "OR (a.endTime != :seriesEndTime)) " +
            "AND ((a.startTime <= :endTime AND a.endTime >= :startTime))")
    List<Appointment> findConflictingAppointmentsExcludingSeries(
            @Param("roomId") int roomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("seriesRoom") Room seriesRoom,
            @Param("seriesStartTime") LocalTime seriesStartTime,
            @Param("seriesEndTime") LocalTime seriesEndTime
    );
    @Query("SELECT a.id FROM Appointment a WHERE a.id IN :ids")
    List<Integer> findAllExistingIds(List<Integer> ids);
    @Query("SELECT a FROM Appointment a WHERE a.room.id = :roomId AND a.date = :date ORDER BY a.startTime")
    List<Appointment> findByRoomAndDate(@Param("roomId") Integer roomId, @Param("date") LocalDate date);
}
