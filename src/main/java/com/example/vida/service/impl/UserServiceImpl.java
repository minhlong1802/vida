package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.entity.Department;
import com.example.vida.entity.User;
import com.example.vida.exception.ImportUserValidationException;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
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
    public UserDetails getUserByEmailAndPassword(String email, String password){
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



    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "0\\d{9}";
        return phoneNumber.matches(phoneRegex);
    }

    @Override
    public User updateUser(Integer id, CreateUserDto createUserDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        updateUserFromRequest(user, createUserDto);

        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatorId(UserContext.getUser().getUserId()); // Assuming the updator ID is 1, you might want to get this from authenticated user
        user.setUpdatorName(UserContext.getUser().getUsername()); // Assuming the updator name is Admin, you might want to get this from authenticated user

        User updatedUser = userRepository.save(user);
        return updatedUser;
    }

    @Override
    public User deleteUser(Integer id) throws UserNotFoundException {

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

    public void deleteUsers(List<Integer> ids) throws UserNotFoundException {
        List<Integer> notFoundIds = new ArrayList<>();
        List<Integer> existingIds = new ArrayList<>();

        for (Integer id : ids) {
            if (!userRepository.existsById(id)) {
                notFoundIds.add(id);
            } else {
                existingIds.add(id);
            }
        }
        if (!notFoundIds.isEmpty()) {
            throw new UserNotFoundException("Users not found with ids: " + notFoundIds);
        }
        userRepository.deleteAllById(existingIds);
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

    public boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" );
    }

    @Override
    public void saveUsersToDatabase(MultipartFile file) {
        if(isValidExcelFile(file)){
            try {
                List<User> users = getUsersDataFromExcel(file.getInputStream());
                this.userRepository.saveAll(users);
            } catch (IOException e) {
                throw new IllegalArgumentException("The file is not a valid excel file");
            }
        }
    }


    public List<User> getUsersDataFromExcel(InputStream inputStream) {
        List<User> users = new ArrayList<>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("user");
            int rowIndex = 0;
            for (Row row : sheet) {
                if (rowIndex == 0) {
                    rowIndex++;
                    continue;
                }
                Iterator<Cell> cellIterator = row.iterator();
                int cellIndex = 0;
                User user = new User();
                CreateUserDto createUserDto = new CreateUserDto(); // Create a new CreateUserDto object
                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    switch (cellIndex) {
                        case 0 -> {
                            String userName = cell.getStringCellValue();
                            user.setUsername(userName);
                            createUserDto.setUsername(userName); // Set username in DTO
                        }
                        case 1 -> {
                            String email = cell.getStringCellValue();
                            user.setEmail(email);
                            createUserDto.setEmail(email); // Set email in DTO
                        }
                        case 2 -> {
                            String departmentName = cell.getStringCellValue();
                            List<Department> departments = departmentRepository.findByName(departmentName);
                            Department department = null;
                            if (!CollectionUtils.isEmpty(departments)) {
                                department = departments.get(0);
                            }
                            user.setDepartment(department);
                        }
                        case 3 -> {
                            LocalDate dob = cell.getLocalDateTimeCellValue().toLocalDate();
                            user.setDob(dob);
                            createUserDto.setDob(dob); // Set DOB in DTO
                        }
                        case 4 -> {
                            String phoneNumber = ((XSSFCell) cell).getRawValue();
                            user.setPhoneNumber(phoneNumber);
                            createUserDto.setPhoneNumber(phoneNumber); // Set phone number in DTO
                        }
                        case 5 -> {
                            String gender = cell.getStringCellValue();
                            user.setGender(gender);
                            createUserDto.setGender(gender); // Set gender in DTO
                        }
                        case 6 -> {
                            String employeeId = cell.getStringCellValue();
                            user.setEmployeeId(employeeId);
                            createUserDto.setEmployeeId(employeeId); // Set employee ID in DTO
                        }
                        case 7 -> {
                            String cardId = cell.getStringCellValue();
                            user.setCardId(cardId);
                            createUserDto.setCardId(cardId); // Set card ID in DTO
                        }
                        default -> {
                        }
                    }
                    cellIndex++;
                }
                Map<String, String> validationErrors = validateUserData(createUserDto);
                if (!CollectionUtils.isEmpty(validationErrors)) {
                    StringBuilder sb = new StringBuilder();
                    for (rowIndex = 1; rowIndex < validationErrors.size(); rowIndex++) {
                        sb.append("Validation errors in row " + rowIndex + ": " + validationErrors + ".").append("\n");
                    }
                    throw new ImportUserValidationException(sb.toString());
                }
                // validate row
                user.setPassword(generatePassword(createUserDto));
                user.setStatus(1);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setCreatorId(UserContext.getUser().getUserId());
                user.setUpdatorId(UserContext.getUser().getUserId());
                user.setCreatorName(UserContext.getUser().getUsername());
                user.setUpdatorName(UserContext.getUser().getUsername());
                users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
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


    private void updateUserFromRequest(User user, CreateUserDto createUserDto) {
        if (createUserDto.getUsername() != null) user.setUsername(createUserDto.getUsername());
        if (createUserDto.getEmail() != null) user.setEmail(createUserDto.getEmail());
        if (createUserDto.getDepartmentId() != null) {

            //tim trong db ban ghi department vs id la request.getDepartmentId()
            //....
            Optional<Department> department = departmentRepository.findById(createUserDto.getDepartmentId());
            user.setDepartment(department.get());
        }
        if (createUserDto.getStatus() != null) user.setStatus(createUserDto.getStatus());
        if (createUserDto.getDob() != null) user.setDob(createUserDto.getDob());
        if (createUserDto.getPhoneNumber() != null) user.setPhoneNumber(createUserDto.getPhoneNumber());
        if (createUserDto.getGender() != null) user.setGender(createUserDto.getGender());
        if (createUserDto.getEmployeeId() != null) user.setEmployeeId(createUserDto.getEmployeeId());
        if (createUserDto.getCardId() != null) user.setCardId(createUserDto.getCardId());
    }


}