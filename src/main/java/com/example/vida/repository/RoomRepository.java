package com.example.vida.repository;

import com.example.vida.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;


public interface RoomRepository extends CrudRepository<Room, Long> {
    Page<Room> findRoomByName(String name, Pageable pageable);
}
