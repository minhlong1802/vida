package com.example.vida.service.impl;

import com.example.vida.repository.UserRepository;
import com.example.vida.utils.PasswordEncoder;

public class UserServiceImplBuilder {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImplBuilder setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
        return this;
    }

    public UserServiceImplBuilder setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        return this;
    }

    public UserServiceImpl createUserServiceImpl() {
        return new UserServiceImpl(userRepository, passwordEncoder);
    }
}