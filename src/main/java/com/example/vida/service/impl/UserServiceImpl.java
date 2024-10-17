package com.example.vida.service.impl;

import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.LoginResponse;
import com.example.vida.entity.User;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public LoginResponse getUserByEmailAndPassword(LoginRequest loginRequest){
        User user = userRepository.findUserByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (loginRequest.getPassword().equals(user.getPassword())) {
            String token = "dummy-token";
            return new LoginResponse(token);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

}
