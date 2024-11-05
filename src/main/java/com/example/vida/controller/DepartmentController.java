package com.example.vida.controller;

import com.example.vida.dto.response.APIResponse;
import com.example.vida.service.DepartmentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("api/departments")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    @GetMapping()
    public ResponseEntity<Object> searchDepartments(@RequestParam String searchText,
                                                         @RequestParam @Nullable Integer companyId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> mapDepartment = departmentService.searchDepartmentsByName(searchText, companyId, page, size);
            return APIResponse.responseBuilder(mapDepartment, null, HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}