package com.example.vida.service;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.vida.dto.request.UpdateUserRequest;
import com.example.vida.dto.response.UserResponse;

public interface UserService {
    UserDetails getUserByEmailAndPassword(String email, String password);
    UserResponse createUser(CreateUserDto createUserDto);
    UserResponse updateUser(Integer id, UpdateUserRequest request);
}
