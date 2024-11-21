package com.example.vida.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class ImportUsersDto {
    @Valid
    private List<CreateUserDto> users;
}