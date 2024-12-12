package com.example.vida.service;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.DeleteRequest;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {
    UserDetails getUserByEmailAndPassword(String email, String password);
    com.example.vida.entity.User createUser(CreateUserDto createUserDto);
    com.example.vida.entity.User updateUser(Integer id, CreateUserDto updateUserDto) throws UserNotFoundException;
    User deleteUser(Integer id) throws UserNotFoundException;
    com.example.vida.entity.User getUserById(Integer id) throws UserNotFoundException;
    Map<String, Object> searchUsersByName(String searchText, Integer companyId, Integer departmentId, Integer status, Integer page, Integer size);

    void deleteUsers(DeleteRequest request) throws UserNotFoundException;
    byte[] exportUsers(String searchText,Integer companyId, Integer departmentId, Integer status);

    Map<String, String> validateUserData(@Valid CreateUserDto createUserDto, String mode);

    Object saveUsersToDatabase(MultipartFile file);


}