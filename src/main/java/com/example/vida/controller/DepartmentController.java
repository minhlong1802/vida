package com.example.vida.controller;

import com.example.vida.entity.Department;
import com.example.vida.service.DepartmentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    //Get All Department
    //http://localhost:8080/api/departments
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartment() {
        List<Department> departments = departmentService.getAllDepartments();
        return new ResponseEntity<>(departments, HttpStatus.OK);
    }
    @GetMapping("/search")
    public ResponseEntity<Page<Department>> searchDepartments(@RequestParam String searchText,
                                                              @RequestParam(defaultValue = "0") Integer page,
                                                              @RequestParam(defaultValue = "3") Integer size) {
        Page<Department> departments = departmentService.searchDepartmentsByName(searchText);
        return ResponseEntity.ok(departments);
    }
}