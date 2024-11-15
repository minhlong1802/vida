package com.example.vida.repository;

import com.example.vida.entity.Company;
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

import java.util.Collection;
import java.util.List;

@Repository
public interface DepartmentRepository extends CrudRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    @Query(nativeQuery = true, value= "SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchText, '%'))" )
    List<Department> searchDepartmentsByName(@Param("searchText") String searchText);

    List<Department> findByCompany(Company company);
}