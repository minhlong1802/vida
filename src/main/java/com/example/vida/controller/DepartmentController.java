package com.example.vida.controller;

import com.example.vida.entity.Department;
import com.example.vida.service.DepartmentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/departments")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping()
    public ResponseEntity<Page<Department>> searchDepartments(@RequestParam String searchText,
                                                              @RequestParam @Nullable Integer companyId,
                                                              @RequestParam(defaultValue = "1") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        Page<Department> departments = departmentService.searchDepartmentsByName(searchText, companyId, page, size);
        return ResponseEntity.ok(departments);
    }
}