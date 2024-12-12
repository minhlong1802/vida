package com.example.vida.service;

import com.example.vida.dto.request.RequestAppointmentDto;
import com.example.vida.entity.Appointment;
import com.example.vida.entity.Room;
import com.example.vida.entity.User;
import com.example.vida.exception.RoomNotFoundException;
import com.example.vida.exception.UserNotFoundException;
import com.example.vida.repository.RoomRepository;
import com.example.vida.repository.UserRepository;
import com.example.vida.utils.UserContext;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    private final String fromEmail;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendAppointmentNotification(Appointment appointment, RequestAppointmentDto requestDto) {
        try {
            // Retrieve current user details
            User currentUser = userRepository.findById(UserContext.getUser().getUserId())
                    .orElseThrow(() -> new UserNotFoundException("Current user not found"));

            // Initialize users set and add creator
            Set<User> users = new HashSet<>();
            users.add(currentUser);

            // Add additional users
            if (requestDto.getUserIds() != null && !requestDto.getUserIds().isEmpty()) {
                Set<User> additionalUsers = requestDto.getUserIds().stream()
                        .filter(userId -> !userId.equals(currentUser.getId())) // Skip if creator is already in the list
                        .map(userId -> userRepository.findById(userId)
                                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)))
                        .collect(Collectors.toSet());
                users.addAll(additionalUsers);
                appointment.setUsers(users);
            }

            // Send email to each assigned user
            for (User user : users) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom("nguyenlong28022004@gmail.com", "Test sending email");
                helper.setTo(user.getEmail()); // Send to each user's email
                helper.setSubject("New Appointment assigned");

                String emailContent = buildEmailContent(appointment, requestDto);
                helper.setText(emailContent, true); // true indicates HTML content

                mailSender.send(message);
            }

            log.info("Appointment notification emails sent successfully to {} users", users.size());
        } catch (Exception e) {
            log.error("Failed to send appointment notification emails", e);
        }
    }

    private String buildEmailContent(Appointment appointment, RequestAppointmentDto requestDto) {
        // Retrieve room details once
        Room room = roomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found for ID: " + requestDto.getRoomId()));
        return String.format("""
            <html>
            <body>
                <h2>New Appointment Assigned</h2>
                <p>Appointment Details:</p>
                <ul>
                    <li>Title: %s</li>
                    <li>Room Name: %s</li>
                    <li>Room Location: %s</li>
                    <li>Date: %s</li>
                    <li>Start Time: %s</li>
                    <li>End Time: %s</li>
                    <li>Content Brief: %s</li>
                </ul>
            </body>
            </html>
            """,
                requestDto.getTitle(),
                room.getName(),
                room.getLocation(),
                requestDto.getDate(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getContentBrief()
        );
    }
}