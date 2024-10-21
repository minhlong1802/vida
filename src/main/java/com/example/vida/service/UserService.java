package com.example.vida.service;

import com.example.vida.dto.CreateUserDto;
import com.example.vida.entity.User;

public interface UserService {
    User createUser(CreateUserDto createUserDto);
}