
package com.example.vida.service;

import com.example.vida.dto.request.CreateDepartmentDto;
import com.example.vida.entity.Department;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    //Get All Department by CompanyId:
    List<Department> getDepartmentsByCompanyId(Long companyId);
    Map<String, Object> searchDepartments(String searchText, Integer companyId, int page, int size);

    Department postDepartment(CreateDepartmentDto createDepartmentDto);

    Department updateDepartment(Integer id, CreateDepartmentDto createDepartmentDto);

    Department getDepartmentDetail(Integer id);

    void deleteDepartmentsByIds(List<Long> ids);

}