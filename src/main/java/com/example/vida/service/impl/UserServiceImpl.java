package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.entity.Department;
import com.example.vida.entity.User;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.exception.UserValidationException;
import com.example.vida.repository.DepartmentRepository;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.UserService;
import com.example.vida.utils.UserContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.vida.dto.request.UpdateUserDto;
import com.example.vida.dto.response.UserResponse;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public org.springframework.security.core.userdetails.User getUserByEmailAndPassword(String email, String password){
        User user = userRepository.findUserByEmailAndPassword(email, password);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    @Override
    public User createUser(CreateUserDto createUserDto) {
        validateNewUser(createUserDto);

        User user = new User();
        BeanUtils.copyProperties(createUserDto, user);
        user.setPassword(generatePassword(createUserDto));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        if (createUserDto.getDepartmentId() != null) {
            Optional<Department> department = departmentRepository.findById(createUserDto.getDepartmentId());
            user.setDepartment(department.get());
        }
        user.setCreatorId(UserContext.getUser().getUserId());
        user.setCreatorName(UserContext.getUser().getUsername());
        user.setUpdatorId(UserContext.getUser().getUserId());
        user.setUpdatorName(UserContext.getUser().getUsername());
        User createdUser=userRepository.save(user);
        // map tu entity to response
        return createdUser;
    }



    private String generatePassword(CreateUserDto createUserDto) {
        String formattedDob = createUserDto.getDob().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        return formattedDob;
    }

    private void validateNewUser(CreateUserDto createUserDto) {
//        if (!isValidEmail(createUserDto.getEmail())) {
//            throw new UserValidationException("Invalid email format");
//        }
        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            throw new UserValidationException("Username already exists");
        }
        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            throw new UserValidationException("Email already exists");
        }
        if (!isValidPhoneNumber(createUserDto.getPhoneNumber())) {
            throw new UserValidationException("Invalid phone number format");
        }
    }

//    private boolean isValidEmail(String email) {
//        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
//        return email.matches(emailRegex);
//    }
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "0\\d{9}";
        return phoneNumber.matches(phoneRegex);
    }

    @Override
    public User updateUser(Integer id, UpdateUserDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        updateUserFromRequest(user, request);

        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatorId(UserContext.getUser().getUserId()); // Assuming the updator ID is 1, you might want to get this from authenticated user
        user.setUpdatorName(UserContext.getUser().getUsername()); // Assuming the updator name is Admin, you might want to get this from authenticated user

        User updatedUser = userRepository.save(user);
        return updatedUser;
    }

    @Override
    public org.springframework.security.core.userdetails.User deleteUser(Integer id) throws UserNotFoundException {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        } else {
            userRepository.deleteById(id);
        }
        return null;

    }

    @Override
    public User getUserById(Integer id) throws UserNotFoundException {
        User user = userRepository.findById(id)
               .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return user;
    }

    public void deleteUsers(List<Integer> ids) {
        for (Integer id : ids) {
            if (!userRepository.existsById(id)) {
                throw new UserNotFoundException("User with id " + id + " not found");
            }
        }
        userRepository.deleteAllById(ids);
    }

    @Override
    public Map<String, String> validateUserData(CreateUserDto createUserDto) {
        Map<String, String> errors = new HashMap<>();

        if (!isValidPhoneNumber(createUserDto.getPhoneNumber())) {
            errors.put("phoneNumber", "Invalid phone number format");
        }

        if (userRepository.existsByUsername(createUserDto.getUsername())) {
            errors.put("username", "Username already exists");
        }

        if (userRepository.existsByEmail(createUserDto.getEmail())) {
            errors.put("email", "Email already exists");
        }

        return errors;
    }

    @Override
    public Map<String, Object> searchUsersByName(String searchText, Integer companyId, Integer departmentId, Integer status, Integer page, Integer size) {
        try {
            if (page > 0) {
                page = page - 1;
            }
            Pageable pageable = PageRequest.of(page, size);
            Specification<User> sepecification = new Specification<User>() {
                @Override
                public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    if (searchText != null) {
                        predicates.add(criteriaBuilder.like(root.get("username"), "%" + searchText + "%"));
                    }
                    if (companyId != null) {
                        predicates.add(criteriaBuilder.equal(root.get("department").get("company").get("id"), companyId));
                    }
                    if (departmentId != null) {
                        predicates.add(criteriaBuilder.equal(root.get("department").get("id"),departmentId));
                    }
                    if (status != null) {
                        predicates.add(criteriaBuilder.equal(root.get("status"),status));
                    }
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };

            Page<User> pageUser = userRepository.findAll(sepecification, pageable);
            Map<String, Object> mapUser = new HashMap<>();
            mapUser.put("list", pageUser.getContent());
            mapUser.put("pageSize", pageUser.getSize());
            mapUser.put("pageNo", pageUser.getNumber()+1);
            mapUser.put("totalPage", pageUser.getTotalPages());
            return mapUser;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public byte[] exportUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found to export");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Username", "Email", "Department ID", "Status", "DOB", "Phone Number", "Gender", "Employee ID", "Card ID"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Create data rows
        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getEmail());
//            row.createCell(3).setCellValue(user.getDepartmentId());
            row.createCell(4).setCellValue(user.getStatus());
            row.createCell(5).setCellValue(user.getDob().toString());
            row.createCell(6).setCellValue(user.getPhoneNumber());
            row.createCell(7).setCellValue(user.getGender());
            row.createCell(8).setCellValue(user.getEmployeeId());
            row.createCell(9).setCellValue(user.getCardId());
        }

        // Write workbook to ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error writing workbook to output stream", e);
        }

        return outputStream.toByteArray();
    }

    @Override
    public Map<String, Object> getUsers(String searchText, Integer departmentId, Boolean status, Integer page, Integer size) {
        return new HashMap<>();
    }


    private void updateUserFromRequest(User user, UpdateUserDto request) {
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getDepartmentId() != null) {

            //tim trong db ban ghi department vs id la request.getDepartmentId()
            //....
            Optional<Department> department = departmentRepository.findById(request.getDepartmentId());
            user.setDepartment(department.get());
        }
        if (request.getStatus() != null) user.setStatus(request.getStatus());
        if (request.getDob() != null) user.setDob(request.getDob());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getEmployeeId() != null) user.setEmployeeId(request.getEmployeeId());
        if (request.getCardId() != null) user.setCardId(request.getCardId());
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }


}