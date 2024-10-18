package com.example.vida.service.impl;

import com.example.vida.entity.Department;
import com.example.vida.repository.DepartmentRepository;
import com.example.vida.service.DepartmentService;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> getAllDepartments() {
        Pageable pageable = PageRequest.of(0, 100);
        return (List<Department>) departmentRepository.findAll();
    }

    public Page<Department> searchDepartmentsByName(String searchText) {
        Pageable pageable = PageRequest.of(0, 10);
        return departmentRepository.findDepartmentByName(searchText, pageable);
    }
}
