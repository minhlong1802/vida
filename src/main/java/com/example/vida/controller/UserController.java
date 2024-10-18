package com.example.vida.controller;

import com.example.vida.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.LoginResponse;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtils jwtTokenUtil;

    @Autowired
    private UserServiceImpl userDetailsService;

    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception {
        String email = authenticationRequest.getEmail();
        String password = authenticationRequest.getPassword();

        // Load user details
        final UserDetails userDetails = userDetailsService.getUserByEmailAndPassword(email,password);
        if (userDetails == null) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        // Generate token
        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    private String extractUsernameFromEmail(String email) {
        return email.split("@")[0];
    }
//    String creatorId = userDetails.getUsername();
//
//    // Use creatorId or other user details as needed
//
//        return "User created successfully by " + creatorId;

}