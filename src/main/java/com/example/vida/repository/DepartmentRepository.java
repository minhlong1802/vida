package com.example.vida.repository;

import com.example.vida.entity.Department;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    // Tìm kiếm theo name, description hoặc code chứa searchText (không phân biệt hoa thường)
//    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
//    List<Department> searchDepartmentsByName(@Param("searchText") String searchText);
    @Query(nativeQuery = true, value= "SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))" )
    List<Department> searchDepartmentsByName(@Param("searchText") String searchText);

//    @Query(nativeQuery = false, value= "SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))" )
//    Page<Department> findDepartmentByName(Specification<Department> specification, Pageable pageable);
}