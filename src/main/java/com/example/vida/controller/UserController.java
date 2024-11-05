package com.example.vida.controller;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.request.UpdateUserDto;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.dto.response.UserResponse;
import com.example.vida.entity.User;
import com.example.vida.exception.AppointmentNotFoundException;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.service.UserService;
import com.example.vida.utils.JwtTokenUtils;
import com.example.vida.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin
public class UserController {

    @Autowired
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
        return UserContext.getUser().getUserId();
    }

    @PostMapping("api/users")
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        try {
            User createdUser = userService.createUser(createUserDto);
            return APIResponse.responseBuilder(createdUser, "Success", HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(e.getMessage(),"Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("api/users/{id}")
    public ResponseEntity<Object> updateUser(@Valid @PathVariable Integer id, @RequestBody UpdateUserDto request) {
        try {
            User updatedUser = userService.updateUser(id, request);
            return APIResponse.responseBuilder(updatedUser, "Success", HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("api/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Integer id) {
        try {
            userService.deleteUser(id);
            return APIResponse.responseBuilder(null, "User deleted successfully", HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/api/users")
    public ResponseEntity<Object> deleteUsers(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return APIResponse.responseBuilder(
                    null,
                    "No userId provided",
                    HttpStatus.BAD_REQUEST
            );
        }
        try {
            userService.deleteUsers(ids);
            return APIResponse.responseBuilder(
                    null,
                    "Users deleted successfully",
                    HttpStatus.OK
            );
        } catch (UserNotFoundException e) {
            return APIResponse.responseBuilder(
                    null,
                    e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }
    @GetMapping("api/users")
    public ResponseEntity<Object> getUsers(@RequestParam (required = false) String searchText,
                                                    @RequestParam (required = false) Integer companyId,
                                                    @RequestParam (required = false) Integer departmentId,
                                                    @RequestParam (required = false) Integer status,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> mapUser = userService.searchUsersByName(searchText,companyId,departmentId,status,page,size);
            return APIResponse.responseBuilder(mapUser, null, HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("api/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return APIResponse.responseBuilder(user, null, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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