package com.example.vida.controller;

import com.example.vida.entity.Company;
import com.example.vida.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    public List<Company> getAllTodos() {
        return companyService.getAllCompanies();
    }
}
