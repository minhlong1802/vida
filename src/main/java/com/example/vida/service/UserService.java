package com.example.vida.service;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.UpdateUserRequest;
import com.example.vida.dto.response.UserResponse;
import com.example.vida.exception.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    User getUserByEmailAndPassword(String email, String password);
    UserResponse createUser(CreateUserDto createUserDto);
    UserResponse updateUser(Integer id, UpdateUserRequest request) throws UserNotFoundException;
    void deleteUser(Integer id) throws UserNotFoundException;
    UserResponse getUserById(Integer id) throws UserNotFoundException;
    List<com.example.vida.entity.User> searchUsersByName(String searchText);
    byte[] exportUsers();

    Map<String, Object> getUsers(String searchText, Integer departmentId, Boolean status, Integer page, Integer size);
}