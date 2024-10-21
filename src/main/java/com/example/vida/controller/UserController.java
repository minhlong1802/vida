package com.example.vida.controller;

import com.example.vida.service.impl.UserServiceImpl;
import com.example.vida.utils.UserContext;
import com.example.vida.dto.CreateUserDto;
import com.example.vida.entity.User;
import com.example.vida.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.LoginResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private JwtTokenUtils jwtTokenUtil;

    @Autowired
    private UserServiceImpl userDetailsService;

    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) {
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
    //Example for using UserContext
    @GetMapping( "/hello")
    public int hello() {
        int userId = UserContext.getUser().getUserId();
        return userId;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Validated @RequestBody CreateUserDto createUserDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
        }
        User createdUser = userService.createUser(createUserDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}