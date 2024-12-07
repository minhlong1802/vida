package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateRoomDto;
import com.example.vida.dto.request.DeleteRequest;
import com.example.vida.dto.request.RoomFilterRequest;
import com.example.vida.dto.response.UserDto;
import com.example.vida.entity.Room;
import com.example.vida.exception.RoomNotFoundException;
import com.example.vida.repository.RoomRepository;
import com.example.vida.service.RoomService;
import com.example.vida.utils.UserContext;
import jakarta.persistence.EntityNotFoundException;
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
    @Override
    public Room postRoom(CreateRoomDto createRoomDto) {
        try {
            Room room = new Room();
            UserDto currentUser = UserContext.getUser();

            room.setName(createRoomDto.getRoomName());
            room.setCapacity(createRoomDto.getCapacity());
            room.setLocation(createRoomDto.getLocation());

            room.setCreatorId(currentUser.getUserId());
            room.setCreatorName(currentUser.getUsername());

            room.setUpdatorId(currentUser.getUserId());
            room.setUpdatorName(currentUser.getUsername());
            return roomRepository.save(room);
        } catch (Exception e) {
            log.error("Error creating room", e);
            return null;
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
                        case "searchtext": // Tìm kiếm trong name và location
                            String searchText = "%" + value.toLowerCase() + "%";
                            Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchText);
                            Predicate locationPredicate = cb.like(cb.lower(root.get("location")), searchText);
                            predicates.add(cb.or(namePredicate, locationPredicate));
                            break;

                        case "capacity": // Tìm phòng theo dung lượng chính xác
                            try {
                                Integer capacityValue = Integer.parseInt(value);
                                predicates.add(cb.equal(root.get("capacity"), capacityValue));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid capacity value: {}", value);
                            }
                            break;

                        case "mincapacity": // Tìm phòng có dung lượng tối thiểu
                            try {
                                Integer minCapacity = Integer.parseInt(value);
                                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid min capacity value: {}", value);
                            }
                            break;

                        case "maxcapacity": // Tìm phòng có dung lượng tối đa
                            try {
                                Integer maxCapacity = Integer.parseInt(value);
                                predicates.add(cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid max capacity value: {}", value);
                            }
                            break;

                        default: // Các bộ lọc khác (nếu có)
                            predicates.add(cb.equal(cb.lower(root.get(key)), value.toLowerCase()));
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
    @Override
    public Room getRoomDetail(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tồn tại room với id = " + id));
    }
    @Override
    public Room updateRoom(Integer id, CreateRoomDto createRoomDto) {
        Optional<Room> optionalRoom = roomRepository.findById(id);
        if (optionalRoom.isPresent()) {
            Room existingRoom = optionalRoom.get();
            UserDto currentUser = UserContext.getUser();

            existingRoom.setName(createRoomDto.getRoomName());
            existingRoom.setCapacity(createRoomDto.getCapacity());
            existingRoom.setLocation(createRoomDto.getLocation());

            existingRoom.setUpdatorId(currentUser.getUserId());
            existingRoom.setUpdatorName(currentUser.getUsername());

            return roomRepository.save(existingRoom);
        }
        return null;
    }
    @Override
    public void deleteRoomsByIds(DeleteRequest request) throws RoomNotFoundException {
        List<Room> roomsToDelete = (List<Room>) roomRepository.findAllById(request.getIds());
        if (roomsToDelete.size() != request.getIds().size()) {
            throw new EntityNotFoundException("Some rooms not found");
        }
        roomRepository.deleteAll(roomsToDelete);
    }
}