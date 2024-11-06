package com.example.vida.service.impl;

import com.example.vida.entity.Department;
import com.example.vida.repository.DepartmentRepository;
import com.example.vida.service.DepartmentService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;


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

}