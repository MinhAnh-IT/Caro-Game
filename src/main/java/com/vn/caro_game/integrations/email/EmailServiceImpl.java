package com.vn.caro_game.integrations.email;

import com.vn.caro_game.constants.EmailConstants;
import com.vn.caro_game.constants.MessageConstants;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    String fromEmail;
    
    @Override
    public void sendOtpEmail(String to, String otp, String subject) {
        try {
            String htmlContent = loadEmailTemplate(EmailConstants.OTP_EMAIL_TEMPLATE)
                    .replace(EmailConstants.PLACEHOLDER_OTP, otp);
            
            sendHtmlEmail(to, subject, htmlContent);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new CustomException(StatusCode.EMAIL_SEND_FAILED, e);
        }
    }
    
    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            String htmlContent = loadEmailTemplate(EmailConstants.WELCOME_EMAIL_TEMPLATE)
                    .replace(EmailConstants.PLACEHOLDER_USERNAME, username);
            
            sendHtmlEmail(to, EmailConstants.WELCOME_EMAIL_SUBJECT, htmlContent);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
            // Don't throw exception for welcome email as it's not critical
        }
    }
    
    @Override
    public void sendPasswordChangeNotification(String to, String username) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String htmlContent = loadEmailTemplate(EmailConstants.PASSWORD_CHANGED_EMAIL_TEMPLATE)
                    .replace(EmailConstants.PLACEHOLDER_USERNAME, username)
                    .replace(EmailConstants.PLACEHOLDER_DATE, now.format(DateTimeFormatter.ofPattern(MessageConstants.DATE_FORMAT_PATTERN)))
                    .replace(EmailConstants.PLACEHOLDER_TIME, now.format(DateTimeFormatter.ofPattern(MessageConstants.TIME_FORMAT_PATTERN)));
            
            sendHtmlEmail(to, EmailConstants.PASSWORD_CHANGED_SUBJECT, htmlContent);
            log.info("Password change notification sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password change notification to: {}", to, e);
            // Don't throw exception for notification email as it's not critical
        }
    }
    
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, MessageConstants.CHARSET_UTF8);
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    private String loadEmailTemplate(String templateName) throws IOException {
        ClassPathResource resource = new ClassPathResource(MessageConstants.EMAIL_TEMPLATE_PATH + templateName);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
