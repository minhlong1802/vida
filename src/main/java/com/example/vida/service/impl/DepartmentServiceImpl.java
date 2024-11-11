package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateDepartmentDto;
import com.example.vida.dto.response.UserDto;
import com.example.vida.entity.Company;
import com.example.vida.entity.Department;
import com.example.vida.repository.CompanyRepository;
import com.example.vida.repository.DepartmentRepository;
import com.example.vida.service.DepartmentService;
import com.example.vida.utils.UserContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    @Autowired
    private CompanyRepository companyRepository;


    @Override
    public Map<String, Object> searchDepartmentsByName(String searchText, Integer companyId, int page, int size) {
        try {
            if (page > 0) {
                page = page - 1;
            }
            Pageable pageable = PageRequest.of(page, size);
            Specification<Department> specification = new Specification<Department>() {
                @Override
                public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchText + "%"));
                    if (companyId != null) {
                        predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
                    }
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };

            Page<Department> pageDepartment = departmentRepository.findAll(specification, pageable);
            Map<String, Object> mapDepartment = new HashMap<>();
            mapDepartment.put("listDepartment", pageDepartment.getContent());
            mapDepartment.put("pageSize", pageDepartment.getSize());
            mapDepartment.put("pageNo", pageDepartment.getNumber() + 1);
            mapDepartment.put("totalPage", pageDepartment.getTotalPages());
            return mapDepartment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Department postDepartment(CreateDepartmentDto createDepartmentDto) {
        try {
            Department department = new Department();
            UserDto currentUser = UserContext.getUser();

            department.setName(createDepartmentDto.getDepartmentName());
            Company company = companyRepository.findById(Long.valueOf(createDepartmentDto.getCompanyId())).orElse(null);
            department.setCompany(company);

            department.setCreatorId(currentUser.getUserId());
            department.setCreatorName(currentUser.getUsername());

            department.setUpdatorId(currentUser.getUserId());
            department.setUpdatorName(currentUser.getUsername());
            return departmentRepository.save(department);
        } catch (Exception e) {
            log.error("Error creating department", e);
            return null;
        }
    }

    @Override
    public Department updateDepartment(Integer id, CreateDepartmentDto createDepartmentDto) {
        return null;
    }

    @Override
    public Department getDepartmentDetail(Integer id) {
        return departmentRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new EntityNotFoundException("Không tồn tại department với id = " + id));
    }

    @Override
    public void deleteDepartmentsByIds(List<Integer> ids) {

    }

}