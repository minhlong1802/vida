package com.example.vida.repository;

import com.example.vida.entity.Room;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends CrudRepository<Room, Integer>, JpaSpecificationExecutor<Room> {
    @Query(nativeQuery = false,
            value= "SELECT d FROM Room d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR LOWER(d.location) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Room> searchRoomsByNameOrLocation(@Param("searchText") String searchText);
}

