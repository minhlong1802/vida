package com.example.vida.service;

import com.example.vida.dto.request.RequestAppointmentDto;
import com.example.vida.entity.Appointment;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    private final String fromEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendAppointmentNotification(Appointment appointment, RequestAppointmentDto requestDto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setFrom(fromEmail);
            helper.setFrom("nguyenlong28022004@gmail.com","Test sending email");
            helper.setTo("nguyenlongdang76@gmail.com");
            helper.setSubject("New Appointment Created");

            String emailContent = buildEmailContent(appointment, requestDto);
            helper.setText(emailContent, true); // true indicates HTML content

            mailSender.send(message);
            log.info("Appointment notification email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send appointment notification email", e);
        }
    }

    private String buildEmailContent(Appointment appointment, RequestAppointmentDto requestDto) {
        return String.format("""
            <html>
            <body>
                <h2>New Appointment Created</h2>
                <p>Appointment Details:</p>
                <ul>
                    <li>Title: %s</li>
                    <li>Room ID: %d</li>
                    <li>Date: %s</li>
                    <li>Start Time: %s</li>
                    <li>End Time: %s</li>
                    <li>Content Brief: %s</li>
                    <li>Recurrence Pattern: %s</li>
                    <li>Recurrence End Date: %s</li>
                </ul>
            </body>
            </html>
            """,
                requestDto.getTitle(),
                requestDto.getRoomId(),
                requestDto.getDate(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getContentBrief(),
                requestDto.getRecurrencePattern(),
                requestDto.getRecurrenceEndDate()
        );
    }
}