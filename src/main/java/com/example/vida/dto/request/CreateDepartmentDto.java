package com.example.vida.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDepartmentDto {
    @NotBlank(message = "Department name is required")
    private String departmentName;
    @NotNull(message = "Company Id is required")
    private Integer companyId;

}
