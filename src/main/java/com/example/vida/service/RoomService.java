package com.example.vida.service;

import com.example.vida.dto.request.CreateRoomDto;
import com.example.vida.dto.request.DeleteRequest;
import com.example.vida.dto.request.RoomFilterRequest;
import com.example.vida.entity.Room;
import com.example.vida.exception.RoomNotFoundException;

import java.util.Map;

public interface RoomService {

    Map<String, Object> filterRooms(RoomFilterRequest request);

    Room postRoom(CreateRoomDto createRoomDto);

    Room updateRoom(Integer id, CreateRoomDto createRoomDto);

    Room getRoomDetail(Integer id);

    void deleteRoomsByIds(DeleteRequest request) throws RoomNotFoundException;
}
