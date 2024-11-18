package com.example.vida.controller;

import com.example.vida.dto.request.CreateDepartmentDto;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.entity.Department;
import com.example.vida.exception.UnauthorizedException;
import com.example.vida.service.DepartmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/departments")
@Slf4j
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    @PostMapping
    public ResponseEntity<Object> createDepartment(@RequestBody @Valid CreateDepartmentDto createDepartmentDto, BindingResult bindingResult) {
        try {
            Map<String, String> errors = new HashMap<>();
            if (bindingResult.hasErrors()) {
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );
            }
            if (!errors.isEmpty()) {
                return APIResponse.responseBuilder(
                        errors,
                        "Validation failed",
                        HttpStatus.BAD_REQUEST
                );
            }
            Department department = departmentService.postDepartment(createDepartmentDto);
            if (department != null) {
                return APIResponse.responseBuilder(department, "Department created successfully",HttpStatus.OK);
            }
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access", e);
            return APIResponse.responseBuilder(Collections.singletonList("Unauthorized access"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

    @GetMapping()
    public ResponseEntity<Object> searchDepartments(@RequestParam String searchText,
                                                    @RequestParam @Nullable Integer companyId,
                                                    @RequestParam(defaultValue = "1") Integer pageNo,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Map<String, Object> mapDepartment = departmentService.searchDepartments(searchText, companyId, pageNo, pageSize);
            return APIResponse.responseBuilder(mapDepartment, null, HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-company")
    public ResponseEntity<Object> getDepartmentsByCompanyId(@RequestParam Integer companyId) {
        try {
            List<Integer> departmentIds = departmentService.getDepartmentsByCompanyId(companyId);
            return APIResponse.responseBuilder(departmentIds, null, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return APIResponse.responseBuilder(Collections.emptyList(), e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getDepartmentDetail(@PathVariable Integer id) {
        try {
            Department department = departmentService.getDepartmentDetail(id);
            return APIResponse.responseBuilder(
                    department,
                    null,
                    HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return APIResponse.responseBuilder(
                    Collections.emptyMap(),
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> updateDepartment(@PathVariable Integer id, @RequestBody @Valid CreateDepartmentDto createDepartmentDto, BindingResult bindingResult) {
        try {
            Map<String, String> errors = new HashMap<>();
            if (bindingResult.hasErrors()) {
                bindingResult.getFieldErrors().forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );
            }
            if (!errors.isEmpty()) {
                return APIResponse.responseBuilder(
                        errors,
                        "Validation failed",
                        HttpStatus.BAD_REQUEST
                );
            }
            Department department = departmentService.updateDepartment(id, createDepartmentDto);
            if (department != null) {
                return APIResponse.responseBuilder(department, "Department updated successfully", HttpStatus.OK);
            } else {
                return APIResponse.responseBuilder(null, "ID not found", HttpStatus.NOT_FOUND);
            }
        } catch (UnauthorizedException e) {
            log.error("Unauthorized access", e);
            return APIResponse.responseBuilder(Collections.singletonList("Unauthorized access"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping()
    public ResponseEntity<Object> deleteDepartment(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return APIResponse.responseBuilder(null, "Invalid input format. Please check the request body", HttpStatus.BAD_REQUEST);
        }
        try {
            departmentService.deleteDepartmentsByIds(ids);
            return APIResponse.responseBuilder(null, "Departments deleted successfully", HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return APIResponse.responseBuilder(null, "Some departments not found", HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return APIResponse.responseBuilder(Collections.singletonList("Unauthorized access"), "You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
        }
    }

}