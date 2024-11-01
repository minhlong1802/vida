package com.example.vida.service;

import com.example.vida.dto.request.ChangePasswordRequest;
import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.entity.User;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDetails getUserByEmailAndPassword(String email, String password);
    User createUser(CreateUserDto createUserDto);
}