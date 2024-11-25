package com.example.vida.service.impl;

import com.example.vida.dto.request.CreateUserDto;
import com.example.vida.dto.request.DeleteUsersRequest;
import com.example.vida.entity.Company;
import com.example.vida.entity.Department;
import com.example.vida.entity.User;
import com.example.vida.exception.UpdateUserValidationException;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.exception.ValidationException;
import com.example.vida.repository.CompanyRepository;
import com.example.vida.repository.DepartmentRepository;
import com.example.vida.repository.UserRepository;
import com.example.vida.service.UserService;
import com.example.vida.utils.UserContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.*;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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
    private final CompanyRepository companyRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, DepartmentRepository departmentRepository,CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository=companyRepository;
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

        User user = new User();
        BeanUtils.copyProperties(createUserDto, user);
        user.setStatus(1);
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
        Map<String,String> updateErrors = updateUserFromRequest(user, createUserDto);
        if (!updateErrors.isEmpty()){
            throw new UpdateUserValidationException(updateErrors);
        }

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

    public void deleteUsers(DeleteUsersRequest request) throws UserNotFoundException {
        List<Integer> ids = request.getIds();
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
    public Map<String, String> validateUserData(@Valid CreateUserDto createUserDto, String mode) {
        Map<String, String> errors = new HashMap<>();

        if (!isValidPhoneNumber(createUserDto.getPhoneNumber())) {
            errors.put("phoneNumber", "Invalid phone number format");
        }
        if (mode.equals("create")){
            if (userRepository.existsByUsername(createUserDto.getUsername())) {
                errors.put("username", "Username already exists");
            }

            if (userRepository.existsByEmail(createUserDto.getEmail())) {
                errors.put("email", "Email already exists");
            }
        }
        return errors;
    }

    public boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" );
    }

    @Override
    public Object saveUsersToDatabase(MultipartFile file) {
        if (!isValidExcelFile(file)) {
            HashMap<String, String> errors = new HashMap<>();
            errors.put("error", "The file is not a valid Excel file");
            return errors;
        }

        try {
            Object result = getUsersDataFromExcel(file.getInputStream());

            if (result instanceof HashMap) {
                Map<String, String> validationErrors = (Map<String, String>) result;
                // Create a formatted error message from the validation errors
                StringBuilder errorMessage = new StringBuilder("Validation errors found: \n ");
                for (Map.Entry<String, String> error : validationErrors.entrySet()) {
                    errorMessage.append(error.getKey())
                            .append(" - ")
                            .append(error.getValue())
                            .append("\n");
                }
                return validationErrors;
            }

            if (result instanceof List) {
                List<User> users = (List<User>) result;
                if (users.isEmpty()) {
                    HashMap<String, String> errors = new HashMap<>();
                    errors.put("error", "No valid users found in the Excel file");
                    return errors;
                }
                return null;
                // No need to call saveAll since the getUsersDataFromExcel now handles the saving
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read the Excel file: " + e.getMessage());
        } catch (ValidationException e) {
            throw e;  // Rethrow validation exceptions
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while processing the Excel file: " + e.getMessage());
        }
        return null;
    }

    public static boolean isRowEmpty(Row row) {
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public Object getUsersDataFromExcel(InputStream inputStream) {
        List<User> users = new ArrayList<>();
        Map<String, String> validationErrors = new LinkedHashMap<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("user");
            int rowIndex = 0;

            for (Row row : sheet) {
                if (rowIndex == 0) {
                    rowIndex++;
                    continue;
                }
                if (isRowEmpty(row)) {
                    break;
                }



                User user = new User();
                CreateUserDto createUserDto = new CreateUserDto();
                Iterator<Cell> cellIterator = row.iterator();
                int cellIndex = 0;

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cellIndex) {
                        case 0 -> {
                            String userName = cell.getStringCellValue();
                            user.setUsername(userName);
                            createUserDto.setUsername(userName);
                        }
                        case 1 -> {
                            String email = cell.getStringCellValue();
                            user.setEmail(email);
                            createUserDto.setEmail(email);
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
                            createUserDto.setDob(dob);
                        }
                        case 4 -> {
                            String phoneNumber = cell.getStringCellValue();
                            user.setPhoneNumber(phoneNumber);
                            createUserDto.setPhoneNumber(phoneNumber);
                        }
                        case 5 -> {
                            String gender = cell.getStringCellValue();
                            user.setGender(gender);
                            createUserDto.setGender(gender);
                        }
                        case 6 -> {
                            String employeeId = cell.getStringCellValue();
                            user.setEmployeeId(employeeId);
                            createUserDto.setEmployeeId(employeeId);
                        }
                        case 7 -> {
                            String cardId = cell.getStringCellValue();
                            user.setCardId(cardId);
                            createUserDto.setCardId(cardId);
                        }
                    }
                    cellIndex++;
                }

                // Validate the data using all constraints from CreateUserDto
                Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
                Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(createUserDto);

                if (!violations.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder();
                    for (ConstraintViolation<CreateUserDto> violation : violations) {
                        errorMessage.append(violation.getPropertyPath())
                                .append(": ")
                                .append(violation.getMessage())
                                .append("; ");
                    }
                    validationErrors.put("Row " + (rowIndex + 1), errorMessage.toString());
                }

                // Additional custom validations
                Map<String, String> customErrors = validateUserData(createUserDto,"import");
                if (!customErrors.isEmpty()) {
                    String existingError = validationErrors.get("Row " + (rowIndex + 1));
                    StringBuilder errorMessage = new StringBuilder(existingError != null ? existingError : "");

                    for (Map.Entry<String, String> error : customErrors.entrySet()) {
                        errorMessage.append(error.getKey())
                                .append(": ")
                                .append(error.getValue())
                                .append("; ");
                    }
                    validationErrors.put("Row " + (rowIndex + 1), errorMessage.toString());
                }

                if (validationErrors.isEmpty()) {
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

                rowIndex++;
            }

            // If there are any validation errors, return them
            if (!validationErrors.isEmpty()) {
                return validationErrors;
            }

            // Update existing users and add new ones
            for (User user : users) {
                List<User> existingUsers = userRepository.findByUsernameOrEmail(
                        user.getUsername(), user.getEmail()
                );
                if (existingUsers.size() > 1) {
                    return new HashMap<String, String>() {{
                        put("error", "Duplicate user information");
                    }};
                }
                User existingUser = CollectionUtils.isEmpty(existingUsers)? null : existingUsers.get(0);
                if (existingUser != null) {
                    // Update existing user
                    user.setId(existingUser.getId()); // Preserve the ID
                    userRepository.save(user);
                } else {
                    // Add new user
                    userRepository.save(user);
                }
            }

            return users;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<String, String>() {{
                put("error", "Failed to process Excel file: " + e.getMessage());
            }};
        }
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

    public byte[] exportUsers(String searchText, Integer companyId, Integer departmentId, Integer status) {
        try {
            List<User> users = searchUsersForExport(searchText, companyId, departmentId, status);
            if (departmentId != null) {
                departmentRepository.findById(Long.valueOf(departmentId))
                        .orElseThrow(() -> new ValidationException("department not found"));
            }

            if (companyId != null) {
                companyRepository.findById(Long.valueOf(companyId))
                        .orElseThrow(() -> new ValidationException("company not found"));
            }
            // Generate Excel file from search results
            return generateExcelFile(users);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private List<User> searchUsersForExport(String searchText, Integer companyId, Integer departmentId, Integer status) {
        return userRepository.searchUsersForExport(searchText, companyId, departmentId, status);
    }

    private byte[] generateExcelFile(List<User> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // Create Excel data
            List<List<String>> excelData = new ArrayList<>();

            // Add header row
            excelData.add(Arrays.asList(
                    "Username", "Email", "Department Name", "Company Name",
                    "Status", "DOB", "Phone Number", "Gender",
                    "Employee ID", "Card ID"
            ));

            // Add data rows
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (User user : users) {
                Department department = user.getDepartment();
                Company company = department != null ? department.getCompany() : null;
                String dob = user.getDob() != null ? user.getDob().format(dateFormat) : "";

                excelData.add(Arrays.asList(
                        user.getUsername(),
                        user.getEmail(),
                        department != null ? department.getName() : "",
                        company != null ? company.getName() : "",
                        formatStatus(user.getStatus()),
                        dob,
                        user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                        user.getGender() != null ? user.getGender() : "",
                        user.getEmployeeId() != null ? user.getEmployeeId() : "",
                        user.getCardId() != null ? user.getCardId() : ""
                ));
            }

            // Write to Excel
            Sheet sheet = workbook.createSheet("Users");

            // Apply header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.YELLOW.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Apply data cell styling
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);

            // Write headers with style
            Row headerRow = sheet.createRow(0);
            List<String> headers = excelData.get(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Write data rows
            for (int i = 1; i < excelData.size(); i++) {
                Row row = sheet.createRow(i);
                List<String> rowData = excelData.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(rowData.get(j));
                    cell.setCellStyle(dataCellStyle);
                }
            }

            // Auto-size columns for better readability
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private String formatStatus(Integer status) {
        if (status == null) return "";
        // Adjust this based on your status codes
        return switch (status) {
            case 1 -> "Active";
            case 0 -> "Inactive";
            default -> String.valueOf(status);
        };
    }


    private Map<String, String> updateUserFromRequest(User user, CreateUserDto createUserDto) {
        Map<String, String> updateErrors = new HashMap<String, String>();        // For username update
        if (createUserDto.getUsername() != null) {
            // Only validate and update if username is actually different
            if (!createUserDto.getUsername().equals(user.getUsername())) {
                // Check if new username exists for any other user
                if (userRepository.existsByUsernameAndIdNot(createUserDto.getUsername(), user.getId())) {
                    updateErrors.put("username", "Username already exists");
                }
                user.setUsername(createUserDto.getUsername());
            }
        }

        // For email update
        if (createUserDto.getEmail() != null) {
            // Only validate and update if email is actually different
            if (!createUserDto.getEmail().equals(user.getEmail())) {
                // Check if new email exists for any other user
                if (userRepository.existsByEmailAndIdNot(createUserDto.getEmail(), user.getId())) {
                    updateErrors.put("email", "Email already exists");
                }
                user.setEmail(createUserDto.getEmail());
            }
        }

        // Rest of the fields remain the same
        if (createUserDto.getDepartmentId() != null) {

            //tim trong db ban ghi department vs id la request.getDepartmentId()
            Optional<Department> department = departmentRepository.findById(createUserDto.getDepartmentId());
            user.setDepartment(department.get());
        }
        if (createUserDto.getStatus() != null) user.setStatus(createUserDto.getStatus());
        if (createUserDto.getDob() != null) user.setDob(createUserDto.getDob());
        if (createUserDto.getPhoneNumber() != null) {
            if (!createUserDto.getPhoneNumber().equals(user.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumberAndIdNot(createUserDto.getPhoneNumber(), user.getId())){
                    updateErrors.put("phonenumber", "Phonenumber already exists");
                }
            }
            user.setPhoneNumber(createUserDto.getPhoneNumber());
        }
        if (createUserDto.getGender() != null) user.setGender(createUserDto.getGender());
        if (createUserDto.getEmployeeId() != null) {
            if (!createUserDto.getEmployeeId().equals(user.getEmployeeId())) {
                if (userRepository.existsByEmployeeIdAndIdNot(createUserDto.getEmployeeId(), user.getId())){
                    updateErrors.put("employeeCode", "Employee Code already exists");
                }
            }
            user.setEmployeeId(createUserDto.getEmployeeId());
        }
        if (createUserDto.getCardId() != null) {
            if (!createUserDto.getCardId().equals(user.getCardId())) {
                if (userRepository.existsByCardIdAndIdNot(createUserDto.getCardId(), user.getId())){
                    updateErrors.put("CardId", "CardId already exists");
                }
            }
            user.setCardId(createUserDto.getCardId());
        }
        return updateErrors;
    }
}