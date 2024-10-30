package com.example.vida.service;

import com.example.vida.dto.request.CreateRoomDto;
import com.example.vida.dto.request.RoomFilterRequest;
import com.example.vida.entity.Room;

import java.util.Map;

public interface RoomService {
    Map<String, Object> filterRooms(RoomFilterRequest request);
    boolean postRoom(CreateRoomDto createRoomDto);
    boolean updateRoom(Integer id, CreateRoomDto createRoomDto);
    void deleteRoom(Integer id);
}
