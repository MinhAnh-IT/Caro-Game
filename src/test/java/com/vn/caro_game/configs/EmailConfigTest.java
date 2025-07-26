package com.vn.caro_game.configs;

import com.vn.caro_game.constants.EmailConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailConfig.
 * Tests email configuration and JavaMailSender setup.
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class EmailConfigTest {

    @InjectMocks
    private EmailConfig emailConfig;

    @Test
    void javaMailSender_ShouldReturnValidSender() {
        // Given
        ReflectionTestUtils.setField(emailConfig, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailConfig, "mailPort", 587);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", "test@example.com");
        ReflectionTestUtils.setField(emailConfig, "mailPassword", "testPassword");

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();

        // Then
        assertNotNull(mailSender);
        assertTrue(mailSender instanceof JavaMailSenderImpl);
    }

    @Test
    void javaMailSender_ShouldConfigureHostAndPort() {
        // Given
        String testHost = "smtp.test.com";
        int testPort = 465;
        ReflectionTestUtils.setField(emailConfig, "mailHost", testHost);
        ReflectionTestUtils.setField(emailConfig, "mailPort", testPort);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", "test@example.com");
        ReflectionTestUtils.setField(emailConfig, "mailPassword", "testPassword");

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        // Then
        assertEquals(testHost, impl.getHost());
        assertEquals(testPort, impl.getPort());
    }

    @Test
    void javaMailSender_ShouldConfigureCredentials() {
        // Given
        String testUsername = "test@example.com";
        String testPassword = "testPassword123";
        ReflectionTestUtils.setField(emailConfig, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailConfig, "mailPort", 587);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", testUsername);
        ReflectionTestUtils.setField(emailConfig, "mailPassword", testPassword);

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        // Then
        assertEquals(testUsername, impl.getUsername());
        assertEquals(testPassword, impl.getPassword());
    }

    @Test
    void javaMailSender_ShouldConfigureSmtpProperties() {
        // Given
        ReflectionTestUtils.setField(emailConfig, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailConfig, "mailPort", 587);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", "test@example.com");
        ReflectionTestUtils.setField(emailConfig, "mailPassword", "testPassword");

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        // Then
        var properties = impl.getJavaMailProperties();
        assertEquals("smtp", properties.getProperty("mail.transport.protocol"));
        assertEquals("false", properties.getProperty("mail.debug"));
    }

    @Test
    void javaMailSender_ShouldConfigureAuthenticationProperties() {
        // Given
        ReflectionTestUtils.setField(emailConfig, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailConfig, "mailPort", 587);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", "test@example.com");
        ReflectionTestUtils.setField(emailConfig, "mailPassword", "testPassword");

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        // Then
        var properties = impl.getJavaMailProperties();
        assertEquals(EmailConstants.SMTP_AUTH_VALUE, 
                    properties.getProperty(EmailConstants.SMTP_AUTH_PROPERTY));
    }

    @Test
    void javaMailSender_ShouldConfigureStartTlsProperties() {
        // Given
        ReflectionTestUtils.setField(emailConfig, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailConfig, "mailPort", 587);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", "test@example.com");
        ReflectionTestUtils.setField(emailConfig, "mailPassword", "testPassword");

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        // Then
        var properties = impl.getJavaMailProperties();
        assertEquals(EmailConstants.SMTP_STARTTLS_ENABLE_VALUE, 
                    properties.getProperty(EmailConstants.SMTP_STARTTLS_ENABLE_PROPERTY));
        assertEquals(EmailConstants.SMTP_STARTTLS_REQUIRED_VALUE, 
                    properties.getProperty(EmailConstants.SMTP_STARTTLS_REQUIRED_PROPERTY));
    }

    @Test
    void javaMailSender_ShouldHaveAllRequiredProperties() {
        // Given
        ReflectionTestUtils.setField(emailConfig, "mailHost", "smtp.gmail.com");
        ReflectionTestUtils.setField(emailConfig, "mailPort", 587);
        ReflectionTestUtils.setField(emailConfig, "mailUsername", "test@example.com");
        ReflectionTestUtils.setField(emailConfig, "mailPassword", "testPassword");

        // When
        JavaMailSender mailSender = emailConfig.javaMailSender();
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        // Then
        var properties = impl.getJavaMailProperties();
        assertNotNull(properties.getProperty("mail.transport.protocol"));
        assertNotNull(properties.getProperty(EmailConstants.SMTP_AUTH_PROPERTY));
        assertNotNull(properties.getProperty(EmailConstants.SMTP_STARTTLS_ENABLE_PROPERTY));
        assertNotNull(properties.getProperty(EmailConstants.SMTP_STARTTLS_REQUIRED_PROPERTY));
        assertNotNull(properties.getProperty("mail.debug"));
    }
}
