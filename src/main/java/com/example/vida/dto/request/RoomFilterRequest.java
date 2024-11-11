package com.example.vida.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class RoomFilterRequest {
    private Map<String, String> filters;
    private Integer page = 1;
    private Integer size = 10;
}
