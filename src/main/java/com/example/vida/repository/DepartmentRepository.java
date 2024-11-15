package com.example.vida.repository;

import com.example.vida.entity.Department;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    @Query(nativeQuery = true, value= "SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))" )
    List<Department> searchDepartmentsByName(@Param("searchText") String searchText);

    List<Department> findByName(String name);

}