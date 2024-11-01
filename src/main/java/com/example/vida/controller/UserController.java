package com.example.vida.controller;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.request.UpdateUserRequest;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.dto.response.LoginResponse;
import com.example.vida.dto.response.UserResponse;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.service.UserService;
import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.utils.UserContext;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin
public class UserController {

    @Setter
    @Getter
    private AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtil;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenUtils jwtTokenUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }


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
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        try {
            UserResponse createdUser = userService.createUser(createUserDto);
            return APIResponse.ResponseBuilder(createdUser, "Success", HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.ResponseBuilder(e.getMessage(),"Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("api/users/{id}")
    public ResponseEntity<Object> updateUser(@Valid @PathVariable Integer id, @RequestBody UpdateUserRequest request) {
        try {
            UserResponse updatedUser = userService.updateUser(id, request);
            return APIResponse.ResponseBuilder(updatedUser, "Success", HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("api/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return APIResponse.ResponseBuilder(null, "User deleted successfully", HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("api/users")
    public ResponseEntity<Object> getUsers(@RequestParam (required = false) String searchText,
                                                    @RequestParam (required = false) Integer departmentId,
                                                    @RequestParam (required = false) Boolean status,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<User> mapUser = userService.searchUsersByName(searchText);
            return APIResponse.ResponseBuilder(mapUser, null, HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("api/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Integer id) {
        try {
            UserResponse user = userService.getUserById(id);
            return APIResponse.ResponseBuilder(user, null, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return APIResponse.ResponseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("api/users/export")
    public ResponseEntity<byte[]> exportUsers() {
        try {
            byte[] users = userService.exportUsers();
            Path desktopPath = Paths.get(System.getProperty("user.home"), "Desktop");
            Path filePath = desktopPath.resolve("users.xlsx");

            Files.write(filePath, users);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.xlsx\"")
                    .body(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage().getBytes());
        }
    }
}