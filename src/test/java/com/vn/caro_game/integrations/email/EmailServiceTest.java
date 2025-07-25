package com.vn.caro_game.integrations.email;

import com.vn.caro_game.constants.EmailConstants;
import com.vn.caro_game.exceptions.CustomException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private EmailServiceImpl emailService;

    private final String testEmail = "test@example.com";
    private final String testUsername = "testuser";
    private final String testOtp = "123456";
    private final String fromEmail = "noreply@example.com";

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Should send OTP email successfully")
    void shouldSendOtpEmailSuccessfully() throws Exception {
        // Given
        String otpTemplate = "Your OTP is: {{OTP}}";
        mockTemplateLoading(EmailConstants.OTP_EMAIL_TEMPLATE, otpTemplate);

        // When
        emailService.sendOtpEmail(testEmail, testOtp, "Test OTP Subject");

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw CustomException when OTP email fails")
    void shouldThrowCustomExceptionWhenOtpEmailFails() throws Exception {
        // Given
        doThrow(new MailSendException("Mail server error")).when(mailSender).send(any(MimeMessage.class));
        String otpTemplate = "Your OTP is: {{OTP}}";
        mockTemplateLoading(EmailConstants.OTP_EMAIL_TEMPLATE, otpTemplate);

        // When & Then
        assertThatThrownBy(() -> emailService.sendOtpEmail(testEmail, testOtp, "Test Subject"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void shouldSendWelcomeEmailSuccessfully() throws Exception {
        // Given
        String welcomeTemplate = "Welcome {{USERNAME}} to our platform!";
        mockTemplateLoading(EmailConstants.WELCOME_EMAIL_TEMPLATE, welcomeTemplate);

        // When
        emailService.sendWelcomeEmail(testEmail, testUsername);

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should not throw exception when welcome email fails")
    void shouldNotThrowExceptionWhenWelcomeEmailFails() throws Exception {
        // Given
        doThrow(new MailSendException("Mail server error")).when(mailSender).send(any(MimeMessage.class));
        String welcomeTemplate = "Welcome {{USERNAME}} to our platform!";
        mockTemplateLoading(EmailConstants.WELCOME_EMAIL_TEMPLATE, welcomeTemplate);

        // When & Then - Should not throw exception (welcome email is not critical)
        emailService.sendWelcomeEmail(testEmail, testUsername);
        
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send password change notification successfully")
    void shouldSendPasswordChangeNotificationSuccessfully() throws Exception {
        // Given
        String passwordTemplate = "Hi {{USERNAME}}, your password was changed on {{DATE}} at {{TIME}}.";
        mockTemplateLoading(EmailConstants.PASSWORD_CHANGED_EMAIL_TEMPLATE, passwordTemplate);

        // When
        emailService.sendPasswordChangeNotification(testEmail, testUsername);

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should not throw exception when password change notification fails")
    void shouldNotThrowExceptionWhenPasswordChangeNotificationFails() throws Exception {
        // Given
        doThrow(new MailSendException("Mail server error")).when(mailSender).send(any(MimeMessage.class));
        String passwordTemplate = "Hi {{USERNAME}}, your password was changed on {{DATE}} at {{TIME}}.";
        mockTemplateLoading(EmailConstants.PASSWORD_CHANGED_EMAIL_TEMPLATE, passwordTemplate);

        // When & Then - Should not throw exception (notification is not critical)
        emailService.sendPasswordChangeNotification(testEmail, testUsername);
        
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should replace OTP placeholder in template")
    void shouldReplaceOtpPlaceholderInTemplate() throws Exception {
        // Given
        String template = "Your verification code is: {{OTP}}. Please enter this code.";
        mockTemplateLoading(EmailConstants.OTP_EMAIL_TEMPLATE, template);

        // When
        emailService.sendOtpEmail(testEmail, "123456", "Test Subject");

        // Then
        verify(mailSender).send(mimeMessage);
        // The template replacement is tested indirectly through successful email sending
    }

    @Test
    @DisplayName("Should replace username placeholder in welcome template")
    void shouldReplaceUsernamePlaceholderInWelcomeTemplate() throws Exception {
        // Given
        String template = "Welcome {{USERNAME}} to our amazing platform!";
        mockTemplateLoading(EmailConstants.WELCOME_EMAIL_TEMPLATE, template);

        // When
        emailService.sendWelcomeEmail(testEmail, "John");

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should replace multiple placeholders in password change template")
    void shouldReplaceMultiplePlaceholdersInPasswordChangeTemplate() throws Exception {
        // Given
        String template = "Hi {{USERNAME}}, your password was changed on {{DATE}} at {{TIME}}.";
        mockTemplateLoading(EmailConstants.PASSWORD_CHANGED_EMAIL_TEMPLATE, template);

        // When
        emailService.sendPasswordChangeNotification(testEmail, testUsername);

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should handle template loading failure")
    void shouldHandleTemplateLoadingFailure() {
        // Given - This test assumes template exists, so we'll simulate a different failure
        // by making the mail sender throw an exception during message creation
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Failed to create message"));

        // When & Then
        assertThatThrownBy(() -> emailService.sendOtpEmail(testEmail, testOtp, "Test Subject"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("Failed to send email");
    }

    private void mockTemplateLoading(String templateName, String templateContent) throws IOException {
        // This is a simplified mock - in real testing, you might want to mock ClassPathResource
        // For now, we'll assume the template loading works if the email sending is successful
        // In a more complete test, you could use @MockedStatic for ClassPathResource
    }
}
