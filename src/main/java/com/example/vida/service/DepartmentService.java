package com.example.vida.service;

import com.example.vida.entity.Department;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DepartmentService {
    List<Department> getAllDepartments();

    Page<Department> searchDepartmentsByName(String searchText);
}
