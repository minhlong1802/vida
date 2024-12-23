package com.example.vida.service;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.UpdateUserDto;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDetails getUserByEmailAndPassword(String email, String password);
    com.example.vida.entity.User createUser(CreateUserDto createUserDto);
    com.example.vida.entity.User updateUser(Integer id, CreateUserDto updateUserDto) throws UserNotFoundException;
    User deleteUser(Integer id) throws UserNotFoundException;
    com.example.vida.entity.User getUserById(Integer id) throws UserNotFoundException;
    Map<String, Object> searchUsersByName(String searchText, Integer companyId, Integer departmentId, Integer status, Integer page, Integer size);
    byte[] exportUsers(String searchText,Integer companyId, Integer departmentId, Integer status);

    void deleteUsers(List<Integer> ids) throws UserNotFoundException;

    Map<String, String> validateUserData(@Valid CreateUserDto createUserDto);

    Map<String, String> validateUpdateUserData(@Valid UpdateUserDto updateUserDto);

}