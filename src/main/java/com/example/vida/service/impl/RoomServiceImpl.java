package com.example.vida.service.impl;

import com.example.vida.dto.CreateRoomDto;
import com.example.vida.entity.Room;
import com.example.vida.repository.RoomRepository;
import com.example.vida.service.RoomService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    @Override
    public List<Room> getAllRooms() {
        Pageable pageable = PageRequest.of(0,100);
        return (List<Room>) roomRepository.findAll();
    }

    @Override
    public Room createRoom(CreateRoomDto room) {
        return null;
    }
}
