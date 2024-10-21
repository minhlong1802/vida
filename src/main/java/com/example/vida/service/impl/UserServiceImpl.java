package com.example.vida.service.impl;

import com.example.vida.dto.CreateUserDto;
import com.example.vida.entity.User;
import com.example.vida.exception.UserValidationException;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.UserService;
import com.example.vida.utils.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(CreateUserDto createUserDto) {
        validateNewUser(createUserDto);

        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
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