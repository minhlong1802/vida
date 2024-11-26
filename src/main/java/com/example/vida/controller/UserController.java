package com.example.vida.controller;

import com.example.vida.dto.request.ChangePasswordRequest;
import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.DeleteUsersRequest;
import com.example.vida.dto.request.LoginRequest;
import com.example.vida.dto.response.APIResponse;
import com.example.vida.entity.User;
import com.example.vida.exception.UpdateUserValidationException;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.exception.ValidationException;
import com.example.vida.service.UserService;
import com.example.vida.service.impl.UserDetailServiceImpl;
import com.example.vida.service.impl.UserServiceImpl;
import com.example.vida.utils.JwtTokenUtils;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtil;
    private final UserDetailServiceImpl userDetailServiceImpl;
    private final UserServiceImpl userServiceImpl;

    public UserController(UserService userService, JwtTokenUtils jwtTokenUtil, UserDetailServiceImpl userDetailServiceImpl, UserServiceImpl userServiceImpl) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailServiceImpl = userDetailServiceImpl;
        this.userServiceImpl = userServiceImpl;
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
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserDto createUserDto, BindingResult bindingResult) {
        // Thu thập tất cả các lỗi validation
        Map<String, String> errors = new HashMap<>();

        // 1. Thêm các lỗi từ @Valid annotation
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }

        // 2. Thêm các lỗi từ business validation
        try {
            Map<String, String> businessErrors = userService.validateUserData(createUserDto,"create");
            errors.putAll(businessErrors);
        } catch (Exception e) {
            log.error("Error during business validation", e);
            return APIResponse.responseBuilder(
                    null,
                    "Internal validation error",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // Nếu có bất kỳ lỗi nào, trả về tất cả các lỗi
        if (!errors.isEmpty()) {
            return APIResponse.responseBuilder(
                    errors,  // Trả về map chứa tất cả các lỗi trong data
                    "Validation failed",
                    HttpStatus.BAD_REQUEST
            );
        }
        try {
            User createdUser = userService.createUser(createUserDto);
            return APIResponse.responseBuilder(createdUser, "Success", HttpStatus.OK);
        } catch (Exception e) {
            return APIResponse.responseBuilder(e.getMessage(),"Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("api/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Integer id,@Valid @RequestBody CreateUserDto createUserDto, BindingResult bindingResult) {
        // Thu thập tất cả các lỗi validation
        Map<String, String> errors = new HashMap<>();

        // 1. Thêm các lỗi từ @Valid annotation
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }

        // 2. Thêm các lỗi từ business validation
        try {
            Map<String, String> businessErrors = userService.validateUserData(createUserDto,"update");
            errors.putAll(businessErrors);
        } catch (Exception e) {
            log.error("Error during business validation", e);
            return APIResponse.responseBuilder(
                    null,
                    "Internal validation error",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        // Nếu có bất kỳ lỗi nào, trả về tất cả các lỗi
        if (!errors.isEmpty()) {
            return APIResponse.responseBuilder(
                    errors,  // Trả về map chứa tất cả các lỗi trong data
                    "Validation failed",
                    HttpStatus.BAD_REQUEST
            );
        }
        try {
            User updatedUser = userService.updateUser(id, createUserDto);
            return APIResponse.responseBuilder(updatedUser, "Success", HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return APIResponse.responseBuilder(null, e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (UpdateUserValidationException e){
            return APIResponse.responseBuilder(e.getUpdateErrors(), e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
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
    public ResponseEntity<Object> deleteUsers(@RequestBody DeleteUsersRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return APIResponse.responseBuilder(
                    null,
                    "No userId provided",
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            userService.deleteUsers(request);
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
    public ResponseEntity<Object> getUsers(@RequestParam @Nullable String searchText,
                                           @RequestParam @Nullable Integer companyId,
                                           @RequestParam @Nullable Integer departmentId,
                                           @RequestParam @Nullable Integer status,
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

    @PostMapping("api/users/import")
    public ResponseEntity<Object> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return APIResponse.responseBuilder(
                    null,
                    "Please upload a file",
                    HttpStatus.BAD_REQUEST
            );
        }

        Object result = userService.saveUsersToDatabase(file);

        if (result instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, String> errors = (HashMap<String, String>) result;
            return APIResponse.responseBuilder(
                    errors,
                    "Validation errors occurred",
                    HttpStatus.BAD_REQUEST
            );
        }

        // If result is null, it means success
        return APIResponse.responseBuilder(
                null,
                "Users imported successfully",
                HttpStatus.OK
        );
    }
    @GetMapping(value = "api/users/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer status
    ) {
        try {
            byte[] excelBytes = userService.exportUsers(searchText, companyId, departmentId, status);

            if (excelBytes == null || excelBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("users.xlsx").build());

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage().getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error during exporting file: " + e.getMessage()).getBytes());
        }
    }
}