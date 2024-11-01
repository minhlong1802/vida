package com.example.vida.controller;

import com.example.vida.dto.request.ChangePasswordRequest;
import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.dto.response.LoginResponse;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.exception.ValidationException;
import com.example.vida.service.UserService;
import com.example.vida.service.impl.UserDetailServiceImpl;
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
    private UserDetailServiceImpl userDetailServiceImpl;

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
    @PostMapping("api/auth/change-password")
    public ResponseEntity<Object> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,BindingResult bindingResult
    ) {
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
        try {
            userDetailServiceImpl.changePassword(request);
//            String username=UserContext.getUser().getUsername();
//            final UserDetails userDetails= userDetailServiceImpl.loadUserByUsername(username);
//            final String newToken = jwtTokenUtil.generateToken(userDetails);
            return APIResponse.responseBuilder(
                    null,
                    "Password changed successfully",
                    HttpStatus.OK
            );
        }  catch (ValidationException e) {
            return APIResponse.responseBuilder(
                    null,
                    "Invalid password: "+e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }catch (Exception e) {
            return APIResponse.responseBuilder(
                    null,
                    "An unexpected error occurred",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
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