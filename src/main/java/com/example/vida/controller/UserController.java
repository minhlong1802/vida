package com.example.vida.controller;

import com.example.vida.dto.CreateUserDto;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.LoginResponse;
import com.example.vida.entity.User;
import com.example.vida.service.UserService;
import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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