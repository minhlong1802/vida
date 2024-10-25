package com.example.vida.controller;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.LoginResponse;
import com.example.vida.entity.User;
import com.example.vida.service.UserService;
import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.utils.UserContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.vida.dto.request.UpdateUserRequest;
import com.example.vida.dto.response.UserResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import com.example.vida.dto.response.APIResponse;


@RestController
@CrossOrigin
public class UserController {

    @Setter
    @Getter
    private AuthenticationManager authenticationManager;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenUtils jwtTokenUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    private final JwtTokenUtils jwtTokenUtil;


    @RequestMapping(value = "/api/auth/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) {
        String email = authenticationRequest.getEmail();
        String password = authenticationRequest.getPassword();

        // Load user details
        final UserDetails userDetails = userService.getUserByEmailAndPassword(email,password);
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
        return UserContext.getUser().getUserId();
    }

    @PostMapping("api/users")
    public ResponseEntity<Object> createUser(@Validated @RequestBody CreateUserDto createUserDto, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return APIResponse.ResponseBuilder(bindingResult.getAllErrors(),"Invalid request", HttpStatus.BAD_REQUEST);
            }
            UserResponse createdUser = userService.createUser(createUserDto);
            return APIResponse.ResponseBuilder(createdUser, "create user success", HttpStatus.CREATED);
        } catch (Exception e) {
            return APIResponse.ResponseBuilder(e.getMessage(),"Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("api/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Integer id, @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return APIResponse.ResponseBuilder(ResponseEntity.ok(updatedUser),"User updated successfully",HttpStatus.OK);
    }

}