package com.example.vida.service;

import com.example.vida.dto.CreateRoomDto;
import com.example.vida.entity.Room;

import java.util.List;

public interface RoomService {
    List<Room> getAllRooms();
    Room createRoom(CreateRoomDto room);
}
