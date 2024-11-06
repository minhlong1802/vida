package com.example.vida.controller;

import com.example.vida.dto.request.RoomFilterRequest;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {
    private final RoomService roomService;

    @GetMapping()
    public ResponseEntity<Object> filterRooms(
            @RequestParam Map<String, String> params) {
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
            log.error("Error while filtering rooms", e);
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}