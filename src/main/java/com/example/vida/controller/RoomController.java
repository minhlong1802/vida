package com.example.vida.controller;

import com.example.vida.dto.request.CreateRoomDto;
import com.example.vida.dto.request.RoomFilterRequest;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.entity.Room;
import com.example.vida.exception.UnauthorizedException;
import com.example.vida.repository.RoomRepository;
import com.example.vida.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {
    private final RoomService roomService;
    private final RoomRepository roomRepository;

    @PostMapping
    public ResponseEntity<Object> createRoom(@RequestBody @Valid CreateRoomDto createRoomDto) {
        try {
            Room room = roomService.postRoom(createRoomDto);
            if (room != null) {
                return APIResponse.responseBuilder(room, "Room created successfully", HttpStatus.OK);
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("error", "Invalid data format");
                return APIResponse.responseBuilder(errors, "Invalid request data", HttpStatus.BAD_REQUEST);
            }
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access", e);
            return APIResponse.responseBuilder(Collections.singletonList("Unauthorized access"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping
    public ResponseEntity<Object> filterRooms(@RequestParam Map<String, String> params) {
        try {
            Integer page = Integer.valueOf(params.getOrDefault("page", "1"));
            Integer size = Integer.valueOf(params.getOrDefault("size", "10"));
            params.remove("page");
            params.remove("size");

            RoomFilterRequest request = new RoomFilterRequest();
            request.setFilters(params);
            request.setPage(page);
            request.setSize(size);

            Map<String, Object> result = roomService.filterRooms(request);
            return APIResponse.responseBuilder(result, null, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error filtering rooms", e);
            return APIResponse.responseBuilder(null, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getRoomDetail(@PathVariable Integer id) {
        try {
            Room room = roomService.getRoomDetail(id);
            return APIResponse.responseBuilder(
                    room,
                    null,
                    HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return APIResponse.responseBuilder(
                    Collections.emptyMap(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> updateRoom(@PathVariable Integer id, @RequestBody @Valid CreateRoomDto createRoomDto) {
        try {
            Room room = roomService.updateRoom(id, createRoomDto);
            if (room != null) {
                return APIResponse.responseBuilder(room, "Room updated successfully", HttpStatus.OK);
            } else {
                return APIResponse.responseBuilder(null, "ID not found", HttpStatus.NOT_FOUND);
            }
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access", e);
            return APIResponse.responseBuilder(Collections.singletonList("Unauthorized access"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping()
    public ResponseEntity<Object> deleteRoomsByIds(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return APIResponse.responseBuilder(null, "Invalid input format. Please check the request body", HttpStatus.BAD_REQUEST);
        }

        try {
            roomService.deleteRoomsByIds(ids);
            return APIResponse.responseBuilder(null, "Rooms deleted successfully", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return APIResponse.responseBuilder(null, "Some rooms not found", HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return APIResponse.responseBuilder(Collections.singletonList("Unauthorized access"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
        }
    }

}