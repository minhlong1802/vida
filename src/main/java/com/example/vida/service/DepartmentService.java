
package com.example.vida.service;

import com.example.vida.entity.Department;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DepartmentService {

    Page<Department> searchDepartmentsByName(String searchText, Integer companyId, int page, int size);

}