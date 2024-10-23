package com.example.vida.service.impl;

import com.example.vida.entity.Department;
import com.example.vida.repository.DepartmentRepository;
import com.example.vida.service.DepartmentService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;


    @Override
    public Page<Department> searchDepartmentsByName(String searchText, Integer companyId, int page, int size) {
        try {
            if (page > 0) {
                page = page - 1;
            }
            Pageable pageable = PageRequest.of(page, size);
            Specification<Department> sepecification = new Specification<Department>() {
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

            Page<Department> pageDepartment = departmentRepository.findAll(sepecification, pageable);

            return pageDepartment;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}