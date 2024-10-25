package com.example.vida.controller;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.dto.response.LoginResponse;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.service.UserService;
import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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


    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody LoginRequest authenticationRequest,
                                                       BindingResult bindingResult) {
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> mapError= new HashMap<>();
            bindingResult.getFieldErrors().forEach(e -> {
                mapError.put(e.getField(), e.getDefaultMessage());
            });

            return APIResponse.responseBuilder(
                    mapError,
                    "Invalid input",
                    HttpStatus.BAD_REQUEST
            );
        }
        try{
            String email = authenticationRequest.getEmail();
            String password = authenticationRequest.getPassword();

            // Load user details
            final UserDetails userDetails = userService.getUserByEmailAndPassword(email,password);

            // Generate token
            final String token = jwtTokenUtil.generateToken(userDetails);

            return APIResponse.responseBuilder(
                    token,
                    "Login successfully",
                    HttpStatus.OK
            );
        } catch (UserNotFoundException e) {
            return APIResponse.responseBuilder(
                    null,
                    "User not found",
                    HttpStatus.NOT_FOUND
            );
        }

    }
    //Example for using UserContext
    @GetMapping( "/hello")
    public int hello() {
        int userId = UserContext.getUser().getUserId();
        return userId;
    }

    @PostMapping("api/users")
    public ResponseEntity<?> createUser(@Validated @RequestBody CreateUserDto createUserDto, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return new ResponseEntity<>(bindingResult.getAllErrors(), HttpStatus.BAD_REQUEST);
            }
            User createdUser = userService.createUser(createUserDto);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}