package com.example.vida.repository;

import com.example.vida.entity.Room;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends CrudRepository<Room, Integer>, JpaSpecificationExecutor<Room> {
    @Query(nativeQuery = true, value= "SELECT d FROM Room d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))" )
    List<Room> searchRoomsByName(@Param("searchText") String searchText);
}
