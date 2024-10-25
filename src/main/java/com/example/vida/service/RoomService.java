package com.example.vida.service;

import com.example.vida.dto.request.RoomFilterRequest;

import java.util.Map;

public interface RoomService {
    Map<String, Object> filterRooms(RoomFilterRequest request);
}
