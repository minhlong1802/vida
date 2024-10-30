package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateRoomDto;
import com.example.vida.dto.request.RoomFilterRequest;
import com.example.vida.entity.Room;
import com.example.vida.repository.RoomRepository;
import com.example.vida.service.RoomService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    public boolean postRoom(CreateRoomDto createRoomDto){
        try {
            Room room = new Room();

            room.setName(createRoomDto.getRoomName());
            room.setCapacity(createRoomDto.getCapacity());
            room.setLocation(createRoomDto.getLocation());

            roomRepository.save(room);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> filterRooms(RoomFilterRequest request) {
        try {
            int page = request.getPage() > 0 ? request.getPage() - 1 : 0;
            Pageable pageable = PageRequest.of(page, request.getSize());

            Specification<Room> specification = createDynamicSpecification(request.getFilters());
            Page<Room> pageRoom = roomRepository.findAll(specification, pageable);

            return createResponse(pageRoom);
        } catch (Exception e) {
            log.error("Error while filtering rooms", e);
            throw new RuntimeException("Failed to filter rooms", e);
        }
    }

    private Specification<Room> createDynamicSpecification(Map<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            filters.forEach((key, value) -> {
                if (value != null && !value.trim().isEmpty()) {
                    switch (key.toLowerCase()) {
                        case "name":
                            predicates.add(cb.like(
                                    cb.lower(root.get("name")),
                                    "%" + value.toLowerCase() + "%"
                            ));
                            break;
                        case "location":
//                                predicates.add(cb.equal(
//                                        cb.lower(root.get("location")),
//                                        value.toLowerCase()
//                                ));
                            predicates.add(cb.like(
                                    cb.lower(root.get("location")), "%" + value.toLowerCase() + "%"
                            ));
                            break;
                        case "capacity":
                            try {
                                Integer capacityValue = Integer.parseInt(value);
                                predicates.add(cb.equal(
                                        root.get("capacity"),
                                        capacityValue
                                ));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid capacity value: {}", value);
                            }
                            break;
                        case "mincapacity":
                            try {
                                Integer minCapacity = Integer.parseInt(value);
                                predicates.add(cb.greaterThanOrEqualTo(
                                        root.get("capacity"),
                                        minCapacity
                                ));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid min capacity value: {}", value);
                            }
                            break;
                        case "maxcapacity":
                            try {
                                Integer maxCapacity = Integer.parseInt(value);
                                predicates.add(cb.lessThanOrEqualTo(
                                        root.get("capacity"),
                                        maxCapacity
                                ));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid max capacity value: {}", value);
                            }
                            break;
                    }
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> createResponse(Page<Room> pageRoom) {
        Map<String, Object> response = new HashMap<>();
        response.put("listRoom", pageRoom.getContent());
        response.put("pageSize", pageRoom.getSize());
        response.put("pageNo", pageRoom.getNumber() + 1);
        response.put("totalPage", pageRoom.getTotalPages());
        response.put("totalElements", pageRoom.getTotalElements());
        return response;
    }

    public boolean updateRoom(Integer id, CreateRoomDto createRoomDto) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if(optionalRoom.isPresent()){
            Room existingRoom = optionalRoom.get();

            existingRoom.setName(createRoomDto.getRoomName());
            existingRoom.setCapacity(createRoomDto.getCapacity());
            existingRoom.setLocation(createRoomDto.getLocation());

            roomRepository.save(existingRoom);
            return true;
        }
        return false;
    }

    public void deleteRoom(Integer id){
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if (optionalRoom.isPresent()) {
            roomRepository.deleteById(id);
        } else {
            throw new RuntimeException("Room not found");
        }
    }
}