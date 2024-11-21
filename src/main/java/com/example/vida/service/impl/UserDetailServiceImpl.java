package com.example.vida.service.impl;

import com.example.vida.dto.request.ChangePasswordRequest;
import com.example.vida.dto.response.UserDto;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.exception.ValidationException;
import com.example.vida.repository.UserRepository;
import com.example.vida.utils.UserContext;
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

    public void changePassword(ChangePasswordRequest request) {
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new ValidationException("New password and old password should not match");
        }
        User currentUser = userRepository.findById(UserContext.getUser().getUserId())
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        // Validate old password
        if (!request.getOldPassword().equals(currentUser.getPassword())) {
            throw new ValidationException("Old password is incorrect");
        }

        // Update password
        currentUser.setPassword(request.getNewPassword());
        userRepository.save(currentUser);
    }
}
