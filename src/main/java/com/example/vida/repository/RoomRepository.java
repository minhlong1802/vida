package com.example.vida.repository;

import com.example.vida.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Page<Room> findRoomByName(String name, Pageable pageable);
}
