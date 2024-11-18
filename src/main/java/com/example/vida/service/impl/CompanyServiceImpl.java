package com.example.vida.service.impl;

import com.example.vida.entity.Company;
import com.example.vida.repository.CompanyRepository;
import com.example.vida.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies(){
        return companyRepository.findAll();
    }
}
