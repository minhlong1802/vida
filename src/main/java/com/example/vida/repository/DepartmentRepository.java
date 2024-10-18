package com.example.vida.repository;

import com.example.vida.entity.Department;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRepository extends CrudRepository<Department, Long> {
    // Tìm kiếm theo name, description hoặc code chứa searchText (không phân biệt hoa thường)
//    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
//    List<Department> searchDepartmentsByName(@Param("searchText") String searchText);
    Page<Department> findDepartmentByName(String name, Pageable pageable);
}
