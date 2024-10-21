package com.example.vida.service.impl;

import com.example.vida.dto.CreateUserDto;
import com.example.vida.dto.response.UserDto;
import com.example.vida.entity.User;
import com.example.vida.exception.UserValidationException;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.UserService;
import com.example.vida.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Service
public class UserServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDto loadUserByUsername(String username) throws UserNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found with username: " + username);
        }
        return new UserDto(user.getId(), user.getUsername(), user.getPassword(), new ArrayList<>());
    }
    public UserDetails getUserByEmailAndPassword(String email, String password){
        User user = userRepository.findUserByEmailAndPassword(email, password);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    @Override
    public User createUser(CreateUserDto createUserDto) {
        validateNewUser(createUserDto);

        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setPassword(createUserDto.getPassword());
        user.setEmail(createUserDto.getEmail());
        user.setDepartmentId(createUserDto.getDepartmentId());
        user.setDob(createUserDto.getDob());
        user.setPhoneNumber(createUserDto.getPhoneNumber());
        user.setGender(createUserDto.getGender());
        user.setEmployeeId(createUserDto.getEmployeeId());
        user.setCardId(createUserDto.getCardId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // Set creator and updator information here if available
        user.setCreatorId(100);
        user.setCreatorName("admin");
        user.setUpdatorId(100);
        user.setUpdatorName("admin");
        return userRepository.save(user);
    }

    private void validateNewUser(CreateUserDto createUserDto) {
        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            throw new UserValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new UserValidationException("Email already exists");
        }
    }
}