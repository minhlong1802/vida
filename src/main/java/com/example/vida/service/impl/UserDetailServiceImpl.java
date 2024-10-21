package com.example.vida.service.impl;

import com.example.vida.dto.response.UserDto;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserDetailServiceImpl(UserRepository userRepository) {
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
}
